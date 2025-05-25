package com.olsonsolution.common.spring.application.annotation.migration;

public enum Operation {

    ADD_COLUMN,
    ADD_UNIQUE_CONSTRAINT,
    ADD_FOREIGN_KEY,
    ADD_NOT_NULL_CONSTRAINT,
    MODIFY_DATA_TYPE,
    DEFAULT_VALUE_CHANGE,
    DROP_NULL_CONSTRAINT,
    DROP_FOREIGN_KEY,
    DROP_DEFAULT_VALUE,
    DROP_UNIQUE_CONSTRAINT

}
