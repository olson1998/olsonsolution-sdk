package com.olsonsolution.common.spring.application.datasource.migration.annotation;

public @interface ColumnChanges {

    ColumnChange[] changes();

}
