package com.olsonsolution.common.spring.application.datasource.migration.annotation.processor;

import lombok.Builder;

@Builder
record ConstraintMetadata(String name, Type type) {

    enum Type {
        PRIMARY_KEY,
        NULLABLE_FALSE,
        UNIQUE,
        FOREIGN_KEY
    }


}
