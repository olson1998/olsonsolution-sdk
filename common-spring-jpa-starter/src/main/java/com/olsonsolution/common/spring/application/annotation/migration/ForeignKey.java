package com.olsonsolution.common.spring.application.annotation.migration;

public @interface ForeignKey {

    String name();

    String referenceColumn();

    String referenceTable();

    String version() default "";
}
