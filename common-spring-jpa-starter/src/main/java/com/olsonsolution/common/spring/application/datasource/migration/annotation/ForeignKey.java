package com.olsonsolution.common.spring.application.datasource.migration.annotation;

public @interface ForeignKey {

    String name();

    String referenceColumn();

    String referenceTable();

    String version();
}
