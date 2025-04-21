package com.olsonsolution.common.spring.application.datasource.migration.annotation.processor;

import lombok.Builder;

@Builder
record ConstraintMetadata(String name, Type type) {

    enum Type {
        PRIMARY_KEY,
        NULLABLE,
        UNIQUE,
        FOREIGN_KEY
    }


}
