package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.olsonsolution.common.reflection.domain.port.repository.annotion.processor.MessagePrinter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.apache.commons.lang3.StringUtils;

import javax.tools.Diagnostic;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
final class ChangeLogOrderer {

    private final MessagePrinter messagePrinter;

    LinkedHashSet<String> sortChangeLogs(List<JpaSpecProcedure> procedures) {
        messagePrinter.print(Diagnostic.Kind.NOTE, ChangeLogOrderer.class, "Sorting change logs");
        Set<String> processedChangeLogs = new HashSet<>();
        LinkedHashSet<String> orderedChangeLogs = new LinkedHashSet<>();
        Collection<JpaSpecMetadata> jpaSpecs = CollectionUtils.collect(procedures, JpaSpecProcedure::metadata);
        Map<JpaSpecMetadata, List<ChangeSetOp>> jpaSpecsChangeSetOps = mapJpaSpecChangeSetOps(procedures);
        for (Map.Entry<JpaSpecMetadata, List<ChangeSetOp>> jpaSpecChangeSetOps : jpaSpecsChangeSetOps.entrySet()) {
            JpaSpecMetadata jpaSpec = jpaSpecChangeSetOps.getKey();
            List<ChangeSetOp> changeSetOps = jpaSpecChangeSetOps.getValue();
            collectOrderedChangeLogs(
                    jpaSpec, changeSetOps,
                    orderedChangeLogs, processedChangeLogs,
                    jpaSpecs, jpaSpecsChangeSetOps
            );
        }
        return orderedChangeLogs;
    }

    private void collectOrderedChangeLogs(JpaSpecMetadata jpaSpec, List<ChangeSetOp> changeSetOps,
                                          Set<String> orderedChangeLogs,
                                          Set<String> processedChangeLogs,
                                          Collection<JpaSpecMetadata> jpaSpecs,
                                          Map<JpaSpecMetadata, List<ChangeSetOp>> jpaSpecChangeSetOps) {
        for (ChangeSetOp changeSetOp : changeSetOps) {
            collectOrderedChangeLogs(
                    changeSetOp, jpaSpec, orderedChangeLogs, processedChangeLogs,
                    jpaSpecs, jpaSpecChangeSetOps
            );
        }
    }

    private void collectOrderedChangeLogs(ChangeSetOp changeSetOp, JpaSpecMetadata jpaSpec,
                                          Set<String> orderedChangeLogs,
                                          Set<String> processedChangeLogs,
                                          Collection<JpaSpecMetadata> jpaSpecsMetadata,
                                          Map<JpaSpecMetadata, List<ChangeSetOp>> jpaSpecChangeSetOps) {
        String jpaSpecName = jpaSpec.jpaSpec();
        String changeLog = changeSetOp.id();
        Map<JpaSpecMetadata, Set<ChangeSetOp>> dependsOn = mapJpaSpecDependsOn(
                jpaSpec, changeSetOp,
                jpaSpecsMetadata, jpaSpecChangeSetOps
        );
        for (Map.Entry<JpaSpecMetadata, Set<ChangeSetOp>> jpaSpecDependentChangeLogs : dependsOn.entrySet()) {
            JpaSpecMetadata dependentJpaSpec = jpaSpecDependentChangeLogs.getKey();
            Set<ChangeSetOp> dependentChangeLogs = jpaSpecDependentChangeLogs.getValue();
            collectDependentChangeLogs(
                    dependentJpaSpec, dependentChangeLogs, orderedChangeLogs,
                    processedChangeLogs, jpaSpecsMetadata, jpaSpecChangeSetOps
            );
        }
        orderedChangeLogs.add(jpaSpecName + '/' + changeLog + ".xml");
        processedChangeLogs.add(changeLog);
    }

    private void collectDependentChangeLogs(JpaSpecMetadata dependentJpaSpec,
                                            Set<ChangeSetOp> dependentChangeLogs,
                                            Set<String> orderedChangeLogs, Set<String> processedChangeLogs,
                                            Collection<JpaSpecMetadata> jpaSpecsMetadata,
                                            Map<JpaSpecMetadata, List<ChangeSetOp>> jpaSpecChangeSetOps) {
        List<ChangeSetOp> changeSetOps = jpaSpecChangeSetOps.get(dependentJpaSpec);
        Objects.requireNonNull(changeSetOps, "ChangeSetOp not found for " + dependentJpaSpec);
        for (ChangeSetOp changeSetOp : dependentChangeLogs) {
            if (processedChangeLogs.contains(changeSetOp.id())) {
                messagePrinter.print(Diagnostic.Kind.NOTE, ChangeLogOrderer.class,
                        "Jpa Spec %s depends on %s %s is already processed".formatted(
                                dependentJpaSpec.jpaSpec(), changeSetOp.id(), dependentJpaSpec.jpaSpec()
                        ));
                return;
            }
            collectOrderedChangeLogs(
                    changeSetOp, dependentJpaSpec, orderedChangeLogs,
                    processedChangeLogs, jpaSpecsMetadata, jpaSpecChangeSetOps
            );
        }
    }

    private Map<JpaSpecMetadata, List<ChangeSetOp>> mapJpaSpecChangeSetOps(List<JpaSpecProcedure> procedures) {
        return procedures.stream()
                .flatMap(procedure -> procedure.changeSets()
                        .stream()
                        .map(c -> new DefaultMapEntry<>(procedure.metadata(), c)))
                .collect(Collectors.groupingBy(
                        DefaultMapEntry::getKey,
                        Collectors.mapping(DefaultMapEntry::getValue, Collectors.toUnmodifiableList())
                ));
    }

    private Map<JpaSpecMetadata, Set<ChangeSetOp>> mapJpaSpecDependsOn(
            JpaSpecMetadata jpaSpecMetadata, ChangeSetOp changeSetOp, Collection<JpaSpecMetadata> jpaSpecsMetadata,
            Map<JpaSpecMetadata, List<ChangeSetOp>> jpaSpecsChangeSetOps) {
        String table = changeSetOp.table();
        EntityConfig entityConfig = jpaSpecMetadata.entitiesConfig()
                .stream()
                .filter(entity -> StringUtils.equals(table, entity.table()))
                .findFirst()
                .orElseThrow();
        List<ChangeSetOp> tableChangeSetOps = jpaSpecsChangeSetOps.entrySet()
                .stream()
                .filter(jpaSpecChangeSetOps ->
                        jpaSpecsChangeSetOps.containsKey(jpaSpecChangeSetOps.getKey())
                ).map(Map.Entry::getValue)
                .flatMap(List::stream)
                .filter(op -> StringUtils.equals(op.table(), table))
                .toList();
        Stream<Map.Entry<JpaSpecMetadata, ChangeSetOp>> dependsOn =
                streamDependsOn(changeSetOp, jpaSpecsMetadata, jpaSpecsChangeSetOps);
        Stream<Map.Entry<JpaSpecMetadata, ChangeSetOp>> oldVersionsChangeLogs = streamOldVersionsChangeLogs(
                changeSetOp, jpaSpecMetadata,
                entityConfig, tableChangeSetOps
        );
        Map<JpaSpecMetadata, Set<ChangeSetOp>> v = Stream.concat(dependsOn, oldVersionsChangeLogs)
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
                ));
        v.forEach((s, c) -> c.forEach(op -> {
            messagePrinter.print(Diagnostic.Kind.NOTE, ChangeLogOrderer.class, "Jpa Spec %s depends on %s %s".formatted(
                    changeSetOp.id(), s.jpaSpec(), op.id()
            ));
        }));
        return v;
    }

    private Stream<Map.Entry<JpaSpecMetadata, ChangeSetOp>> streamOldVersionsChangeLogs(
            ChangeSetOp changeSetOp, JpaSpecMetadata jpaSpecMetadata, EntityConfig entityConfig,
            List<ChangeSetOp> tableChangeSetOps) {
        String version = changeSetOp.version();
        Set<String> versionChronology = entityConfig.versionChronology();
        Iterator<String> versionIterator = versionChronology.iterator();
        Stream.Builder<String> olderVersions = Stream.builder();
        String currentVersion = null;
        while (versionIterator.hasNext() && !StringUtils.equals(currentVersion, version)) {
            currentVersion = versionIterator.next();
            if (!StringUtils.equals(currentVersion, version)) {
                olderVersions.add(currentVersion);
            }
        }
        return olderVersions.build()
                .flatMap(ver -> tableChangeSetOps.stream()
                        .filter(op -> StringUtils.equals(op.version(), ver))
                        .findFirst()
                        .stream())
                .map(op -> new DefaultMapEntry<>(jpaSpecMetadata, op));

    }

    private Stream<Map.Entry<JpaSpecMetadata, ChangeSetOp>> streamDependsOn(
            ChangeSetOp changeSetOp, Collection<JpaSpecMetadata> jpaSpecsMetadata,
            Map<JpaSpecMetadata, List<ChangeSetOp>> jpaSpecsChangeSetOps) {
        return MapUtils.emptyIfNull(changeSetOp.dependsOn())
                .entrySet()
                .stream()
                .flatMap(dependsOn -> streamDependsOn(
                        dependsOn,
                        jpaSpecsMetadata,
                        jpaSpecsChangeSetOps
                ));
    }

    private Stream<Map.Entry<JpaSpecMetadata, ChangeSetOp>> streamDependsOn(
            Map.Entry<String, Set<String>> dependsOn, Collection<JpaSpecMetadata> jpaSpecs,
            Map<JpaSpecMetadata, List<ChangeSetOp>> jpaSpecsChangeSetOps) {
        Set<String> dependsOnChangeLogs = dependsOn.getValue();
        JpaSpecMetadata jpaSpec = jpaSpecs.stream()
                .filter(spec -> StringUtils.equals(dependsOn.getKey(), spec.jpaSpec()))
                .findFirst()
                .orElseThrow();
        return Optional.ofNullable(jpaSpecsChangeSetOps.get(jpaSpec))
                .stream()
                .flatMap(List::stream)
                .filter(changeSetOp -> dependsOnChangeLogs.contains(changeSetOp.id()))
                .map(changeSetOp -> new DefaultMapEntry<>(jpaSpec, changeSetOp));
    }

}
