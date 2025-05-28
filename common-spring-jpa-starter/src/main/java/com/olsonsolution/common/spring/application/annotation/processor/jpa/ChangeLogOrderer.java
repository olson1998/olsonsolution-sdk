package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.olsonsolution.common.reflection.domain.port.repository.annotion.processor.MessagePrinter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.apache.commons.lang3.StringUtils;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

import javax.tools.Diagnostic;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The ChangeLogOrderer class provides functionality to sort and organize a
 * collection of change logs based on their dependencies and predefined order
 * within a list of JPA specification procedures. This ensures that change logs
 * are executed in the correct order without duplicates, while preserving dependencies.
 * <p>
 * This class is immutable and its primary method organizes change logs in a
 * sequential and dependency-respecting manner. Dependency relationships are
 * processed recursively to determine an execution order.
 * <p>
 * Its intended usage is in situations where JPA specification metadata,
 * involving entities, dependencies, and change sets, need to be resolved
 * and sorted for execution.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
final class ChangeLogOrderer {

    private final MessagePrinter messagePrinter;

    /**
     * Sorts and organizes a collection of change logs based on dependencies and specified order
     * within a list of JPA specification procedures. The method returns a linked set containing
     * the change logs in an ordered sequence without duplicates.
     *
     * @param procedures the list of {@link JpaSpecProcedure} objects, each containing metadata
     *                   and its associated change sets from which change logs are to be sorted
     * @return a {@link LinkedHashSet} of change log identifiers sorted in dependency order,
     * preserving the order in which they must be executed
     */
    LinkedHashSet<String> sortChangeLogs(List<JpaSpecProcedure> procedures) {
        messagePrinter.print(Diagnostic.Kind.NOTE, ChangeLogOrderer.class, "Sorting change logs");
        Set<String> processedChangeLogs = new HashSet<>();
        Graph<DefaultMapEntry<JpaSpecMetadata, ChangeSetOp>, DefaultEdge> jpaSpecChangeLogGraph =
                new DefaultDirectedGraph<>(DefaultEdge.class);
        Collection<JpaSpecMetadata> jpaSpecs = CollectionUtils.collect(procedures, JpaSpecProcedure::metadata);
        Map<JpaSpecMetadata, List<ChangeSetOp>> jpaSpecsChangeSetOps = mapJpaSpecChangeSetOps(procedures);
        for (Map.Entry<JpaSpecMetadata, List<ChangeSetOp>> jpaSpecChangeSetOps : jpaSpecsChangeSetOps.entrySet()) {
            JpaSpecMetadata jpaSpec = jpaSpecChangeSetOps.getKey();
            List<ChangeSetOp> changeSetOps = jpaSpecChangeSetOps.getValue();
            collectOrderedChangeLogs(
                    jpaSpec, changeSetOps,
                    jpaSpecChangeLogGraph, processedChangeLogs,
                    jpaSpecs, jpaSpecsChangeSetOps
            );
        }
        Stream.Builder<String> orderedChangeLogs = Stream.builder();
        Iterator<DefaultMapEntry<JpaSpecMetadata, ChangeSetOp>> changeSetOpIterator =
                new TopologicalOrderIterator<>(jpaSpecChangeLogGraph);
        while (changeSetOpIterator.hasNext()) {
            DefaultMapEntry<JpaSpecMetadata, ChangeSetOp> jpaSpecChangeSetOp = changeSetOpIterator.next();
            JpaSpecMetadata jpaSpec = jpaSpecChangeSetOp.getKey();
            ChangeSetOp changeSetOp = jpaSpecChangeSetOp.getValue();
            orderedChangeLogs.add(jpaSpec.jpaSpec() + '/' + changeSetOp.id() + ".xml");
        }
        return orderedChangeLogs.build()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void collectOrderedChangeLogs(JpaSpecMetadata jpaSpec, List<ChangeSetOp> changeSetOps,
                                          Graph<DefaultMapEntry<JpaSpecMetadata, ChangeSetOp>, DefaultEdge> graph,
                                          Set<String> processedChangeLogs,
                                          Collection<JpaSpecMetadata> jpaSpecs,
                                          Map<JpaSpecMetadata, List<ChangeSetOp>> jpaSpecChangeSetOps) {
        Stream.Builder<DefaultMapEntry<JpaSpecMetadata, ChangeSetOp>> independent = Stream.builder();
        for (ChangeSetOp changeSetOp : changeSetOps) {
            collectOrderedChangeLogs(
                    changeSetOp, jpaSpec, graph, independent, processedChangeLogs,
                    jpaSpecs, jpaSpecChangeSetOps
            );
        }
        Set<DefaultMapEntry<JpaSpecMetadata, ChangeSetOp>> independentChangeLogs = independent.build()
                .sorted(Comparator.comparing(j -> j.getKey().jpaSpec()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Iterator<DefaultMapEntry<JpaSpecMetadata, ChangeSetOp>> independentIterator = independentChangeLogs.iterator();
        while (independentIterator.hasNext()) {
            DefaultMapEntry<JpaSpecMetadata, ChangeSetOp> independentChangeLog = independentIterator.next();
            messagePrinter.print(Diagnostic.Kind.NOTE, ChangeLogOrderer.class, "independent: %s".formatted(independentChangeLog.getValue().id()));
            graph.addVertex(independentChangeLog);
            if (independentIterator.hasNext()) {
                DefaultMapEntry<JpaSpecMetadata, ChangeSetOp> nextIndependentChangeLog = independentIterator.next();
                messagePrinter.print(Diagnostic.Kind.NOTE, ChangeLogOrderer.class, "next independent: %s".formatted(nextIndependentChangeLog.getValue().id()));
                graph.addVertex(nextIndependentChangeLog);
                graph.addEdge(independentChangeLog, nextIndependentChangeLog);
            }
        }
    }

    private void collectOrderedChangeLogs(ChangeSetOp changeSetOp, JpaSpecMetadata jpaSpec,
                                          Graph<DefaultMapEntry<JpaSpecMetadata, ChangeSetOp>, DefaultEdge> graph,
                                          Stream.Builder<DefaultMapEntry<JpaSpecMetadata, ChangeSetOp>> independent,
                                          Set<String> processedChangeLogs,
                                          Collection<JpaSpecMetadata> jpaSpecsMetadata,
                                          Map<JpaSpecMetadata, List<ChangeSetOp>> jpaSpecChangeSetOps) {
        String changeLog = changeSetOp.id();
        Map<JpaSpecMetadata, Set<ChangeSetOp>> dependsOn = mapJpaSpecDependsOn(
                jpaSpec, changeSetOp,
                jpaSpecsMetadata, jpaSpecChangeSetOps
        );
        DefaultMapEntry<JpaSpecMetadata, ChangeSetOp> jpaSpecChangeSet = new DefaultMapEntry<>(jpaSpec, changeSetOp);
        if (dependsOn.isEmpty()) {
            independent.add(jpaSpecChangeSet);
            return;
        } else {
            graph.addVertex(jpaSpecChangeSet);
        }
        for (Map.Entry<JpaSpecMetadata, Set<ChangeSetOp>> jpaSpecDependentChangeLogs : dependsOn.entrySet()) {
            JpaSpecMetadata dependentJpaSpec = jpaSpecDependentChangeLogs.getKey();
            Set<ChangeSetOp> dependentChangeLogs = jpaSpecDependentChangeLogs.getValue();
            for (ChangeSetOp dependentChangeLog : dependentChangeLogs) {
                DefaultMapEntry<JpaSpecMetadata, ChangeSetOp> dependantJpaSpecChangeSet = new DefaultMapEntry<>(
                        dependentJpaSpec, dependentChangeLog
                );
                graph.addVertex(dependantJpaSpecChangeSet);
                graph.addEdge(dependantJpaSpecChangeSet, jpaSpecChangeSet);
                messagePrinter.print(
                        Diagnostic.Kind.NOTE, ChangeLogOrderer.class,
                        "Jpa Spec: %s change log: '%s' depends on Jpa Spec: '%s' change log: '%s'".formatted(
                                dependentJpaSpec.jpaSpec(), dependentChangeLog.id(),
                                jpaSpec.jpaSpec(), changeLog
                        )
                );
            }
            collectDependentChangeLogs(
                    jpaSpecChangeSet, dependentJpaSpec, dependentChangeLogs, graph, independent,
                    processedChangeLogs, jpaSpecsMetadata, jpaSpecChangeSetOps
            );
        }
        processedChangeLogs.add(changeLog);
    }

    private void collectDependentChangeLogs(DefaultMapEntry<JpaSpecMetadata, ChangeSetOp> jpaSpecChangeSet,
                                            JpaSpecMetadata dependentJpaSpec,
                                            Set<ChangeSetOp> dependentChangeLogs,
                                            Graph<DefaultMapEntry<JpaSpecMetadata, ChangeSetOp>, DefaultEdge> graph,
                                            Stream.Builder<DefaultMapEntry<JpaSpecMetadata, ChangeSetOp>> independent,
                                            Set<String> processedChangeLogs,
                                            Collection<JpaSpecMetadata> jpaSpecsMetadata,
                                            Map<JpaSpecMetadata, List<ChangeSetOp>> jpaSpecChangeSetOps) {
        List<ChangeSetOp> changeSetOps = jpaSpecChangeSetOps.get(dependentJpaSpec);
        Objects.requireNonNull(changeSetOps, "ChangeSetOp not found for " + dependentJpaSpec);
        for (ChangeSetOp changeSetOp : dependentChangeLogs) {
            if (processedChangeLogs.contains(changeSetOp.id())) {
                DefaultMapEntry<JpaSpecMetadata, ChangeSetOp> dependentJpaSpecChangeSet =
                        new DefaultMapEntry<>(dependentJpaSpec, changeSetOp);
                graph.addEdge(dependentJpaSpecChangeSet, jpaSpecChangeSet);
            }
            collectOrderedChangeLogs(
                    changeSetOp, dependentJpaSpec, graph, independent,
                    processedChangeLogs, jpaSpecsMetadata, jpaSpecChangeSetOps
            );
        }
    }

    /**
     * Maps a list of JPA specification procedures into a grouping of change set operations
     * associated with their respective metadata.
     *
     * @param procedures the list of {@link JpaSpecProcedure} objects, each containing
     *                   metadata and its associated change sets.
     * @return a map where the keys are {@link JpaSpecMetadata} objects representing the metadata
     * of the JPA specification, and the values are immutable lists of {@link ChangeSetOp}
     * representing the change set operations associated with each metadata.
     */
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

    /**
     * Maps the dependencies of a JPA specification metadata to a set of associated change set operations.
     *
     * @param jpaSpecMetadata      the metadata of the JPA specification for which dependencies are to be determined
     * @param changeSetOp          the specific change set operation to analyze dependencies for
     * @param jpaSpecsMetadata     a collection of all available JPA specification metadata
     * @param jpaSpecsChangeSetOps a map containing JPA specification metadata as keys and corresponding
     *                             lists of change set operations as values
     * @return a map where the keys are JPA specification metadata objects that the given JPA specification depends on,
     * and the values are sets of change set operations representing the dependency for each metadata
     */
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

    /**
     * Streams a sequence of older version change logs (as a map entry of JPA specification metadata to change set operation)
     * prior to the current version specified in the provided change set operation.
     *
     * @param changeSetOp       the current {@link ChangeSetOp} instance containing the version to compare against
     * @param jpaSpecMetadata   the {@link JpaSpecMetadata} associated with the specific JPA specification
     * @param entityConfig      the {@link EntityConfig} that holds configuration details including version chronology
     * @param tableChangeSetOps the list of {@link ChangeSetOp} instances representing all operations within a table
     * @return a stream of map entries where the key is the {@link JpaSpecMetadata} and the value is a corresponding
     * {@link ChangeSetOp} for older versions
     */
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

    /**
     * Streams a sequence of dependency entries where each entry represents a mapping
     * between JPA specification metadata and its associated change set operation(s).
     * This method recursively evaluates dependencies based on the provided change set operation.
     *
     * @param changeSetOp          the {@link ChangeSetOp} instance for which dependencies are to be resolved
     * @param jpaSpecsMetadata     a collection of {@link JpaSpecMetadata} representing all available JPA specification metadata
     * @param jpaSpecsChangeSetOps a map containing {@link JpaSpecMetadata} as keys and their corresponding
     *                             lists of {@link ChangeSetOp} operations
     * @return a stream of map entries where each entry consists of a {@link JpaSpecMetadata} key
     * and an associated {@link ChangeSetOp} as the value
     */
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

    /**
     * Streams a sequence of dependency entries where each entry represents a mapping
     * between JPA specification metadata and its associated change set operation(s).
     * This method resolves dependencies based on the provided dependency mapping.
     *
     * @param dependsOn            a map entry where the key is a {@code String} representing the name of the
     *                             JPA specification, and the value is a {@code Set<String>} of change log IDs
     *                             that the JPA specification depends on
     * @param jpaSpecs             a collection of {@link JpaSpecMetadata} representing all available
     *                             JPA specification metadata
     * @param jpaSpecsChangeSetOps a map containing {@link JpaSpecMetadata} keys and their associated
     *                             lists of {@link ChangeSetOp} operations
     * @return a stream of map entries where each entry consists of a {@link JpaSpecMetadata} key
     * and an associated {@link ChangeSetOp} as the value
     */
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
