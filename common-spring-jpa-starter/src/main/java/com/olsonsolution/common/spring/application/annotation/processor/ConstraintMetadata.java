package com.olsonsolution.common.spring.application.annotation.processor;

import lombok.Builder;

import java.util.List;

@Builder
record ConstraintMetadata(String name, Type type, List<String> parameters) {

    enum Type {
        PRIMARY_KEY,
        NON_NULL,
        UNIQUE,
        FOREIGN_KEY
    }


}
