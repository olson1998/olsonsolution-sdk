package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.olsonsolution.common.spring.application.annotation.jpa.EnableJpaSpec;
import com.olsonsolution.common.spring.application.annotation.migration.ColumnChange;
import com.olsonsolution.common.spring.application.annotation.migration.ColumnChanges;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static javax.lang.model.element.ElementKind.FIELD;

record JpaSpecMetadata(@NonNull String jpaSpec,
                       @NonNull TypeElement enableJpaSpecElement,
                       @NonNull String jpaSpecConfigPackage,
                       @JsonInclude(NON_EMPTY) @NonNull Set<EntityConfig> entitiesConfig,
                       @NonNull Set<String> entitiesPackages,
                       @NonNull Set<String> jpaRepositoriesPackages) {

    static final String FIRST_VERSION = "1.0.0";

    public static Builder builder() {
        return new Builder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class Builder {

        private String jpaSpec;

        private TypeElement enableJpaSpecElement;

        private final Set<EntityConfig> entitiesConfig = new HashSet<>();

        Builder jpaSpec(@NonNull String jpaSpec) {
            this.jpaSpec = jpaSpec;
            return this;
        }

        Builder enableJpaSpecElement(@NonNull TypeElement enableJpaSpecElement) {
            this.enableJpaSpecElement = enableJpaSpecElement;
            return this;
        }

        JpaSpecMetadata build(Elements elementUtils) {
            Objects.requireNonNull(jpaSpec, "jpaSpec must not be null");
            Set<String> entitiesBasePackages = entitiesConfig.stream()
                    .map(EntityConfig::entity)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toUnmodifiableList(),
                            entities -> resolveBasePackages(entities, elementUtils)
                    ));
            Set<String> jpaRepositoriesBasesPackages = entitiesConfig.stream()
                    .flatMap(entityConfig -> entityConfig.jpaRepositories().stream())
                    .collect(Collectors.collectingAndThen(
                            Collectors.toUnmodifiableList(),
                            jpaRepositories -> resolveBasePackages(jpaRepositories, elementUtils)
                    ));
            String configPackage = enableJpaSpecElement.getAnnotation(EnableJpaSpec.class).configPackage();
            if (StringUtils.isBlank(configPackage)) {
                configPackage = elementUtils.getPackageOf(enableJpaSpecElement).getQualifiedName().toString();
            }
            return new JpaSpecMetadata(
                    jpaSpec, enableJpaSpecElement, configPackage, entitiesConfig,
                    entitiesBasePackages, jpaRepositoriesBasesPackages
            );
        }

        void appendJpaRepository(TypeElement entityType,
                                 TypeElement jpaRepositoryType,
                                 String table) {
            Optional<EntityConfig> presentEntityConfig = entitiesConfig.stream()
                    .filter(config -> config.entity().equals(entityType))
                    .findFirst();
            EntityConfig entityConfig;
            if (presentEntityConfig.isPresent()) {
                entityConfig = presentEntityConfig.get();
            } else {
                Set<String> versionChronology = collectVersionChronology(entityType);
                entityConfig = EntityConfig.builder()
                        .entity(entityType)
                        .table(table)
                        .jpaRepositories(new ArrayList<>())
                        .versionChronology(versionChronology)
                        .build();
                entitiesConfig.add(entityConfig);
            }
            entityConfig.jpaRepositories().add(jpaRepositoryType);
        }

        private Set<String> resolveBasePackages(Collection<? extends TypeElement> typeElements, Elements elementUtils) {
            return typeElements.stream()
                    .map(typeElement -> resolvePackage(typeElement, elementUtils))
                    .collect(Collectors.collectingAndThen(
                            Collectors.toSet(),
                            this::collectBasePackages
                    ));
        }

        private String resolvePackage(TypeElement typeElement, Elements elementUtils) {
            return elementUtils.getPackageOf(typeElement)
                    .getQualifiedName()
                    .toString();
        }

        private Set<String> collectBasePackages(Set<String> basePackages) {
            Set<String> reducedBasePackages = new HashSet<>();
            for (String basePackage : basePackages) {
                reducedBasePackages.add(basePackage);
                reducedBasePackages.removeIf(p -> !StringUtils.startsWith(p, basePackage));
            }
            return reducedBasePackages;
        }

        private Set<String> collectVersionChronology(TypeElement entityElement) {
            Stream<String> firstVersion = Stream.of(FIRST_VERSION);
            Stream<String> declaredVersions = streamDeclaredFields(entityElement)
                    .filter(field -> field.getAnnotation(ColumnChanges.class) != null)
                    .map(field -> field.getAnnotation(ColumnChanges.class))
                    .flatMap(columnChanges -> Arrays.stream(columnChanges.value()))
                    .map(ColumnChange::ver);
            return Stream.concat(firstVersion, declaredVersions)
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        private Stream<VariableElement> streamDeclaredFields(TypeElement typeElement) {
            return typeElement.getEnclosedElements()
                    .stream()
                    .filter(element -> element.getKind() == FIELD)
                    .filter(VariableElement.class::isInstance)
                    .map(VariableElement.class::cast);
        }

    }

}
