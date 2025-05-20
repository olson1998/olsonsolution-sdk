package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.olsonsolution.common.spring.application.annotation.jpa.EnableJpaSpec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.*;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

record JpaSpecMetadata(@NonNull String jpaSpec,
                       @NonNull TypeElement enableJpaSpecElement,
                       @NonNull String jpaSpecConfigPackage,
                       @JsonInclude(NON_EMPTY) @NonNull Set<EntityConfig> entitiesConfig,
                       @NonNull Set<String> entitiesPackages,
                       @NonNull Set<String> jpaRepositoriesPackages) {

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
                entityConfig = EntityConfig.builder()
                        .entity(entityType)
                        .table(table)
                        .jpaRepositories(new ArrayList<>())
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

    }

}
