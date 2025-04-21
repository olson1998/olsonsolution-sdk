package com.olsonsolution.common.spring.application.datasource.migration.annotation;

public enum Operation {

    ADD_NOT_NULL_CONSTRAINT,
    MODIFY_DATA_TYPE,
    DEFAULT_VALUE_CHANGE,
    DROP_NULL_CONSTRAINT,
    DROP_FOREIGN_KEY,
    DROP_DEFAULT_VALUE,
    DROP_UNIQUE_CONSTRAINT

}
