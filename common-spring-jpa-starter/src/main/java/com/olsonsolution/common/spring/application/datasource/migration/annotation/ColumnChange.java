package com.olsonsolution.common.spring.application.datasource.migration.annotation;

public @interface ColumnChange {

    String column() default "";

    Operation operation();

    ChangeSet changeSet();

}
