package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

record JpaSpecMetadata(@NonNull String jpaSpec,
                       @NonNull TypeElement enableJpaSpecElement,
                       @JsonInclude(NON_EMPTY) @NonNull Set<EntityConfig> entitiesConfig) {

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

        JpaSpecMetadata build() {
            return new JpaSpecMetadata(jpaSpec, enableJpaSpecElement, entitiesConfig);
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

    }

}
