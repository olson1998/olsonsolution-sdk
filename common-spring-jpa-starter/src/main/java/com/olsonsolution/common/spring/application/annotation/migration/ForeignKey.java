package com.olsonsolution.common.spring.application.annotation.migration;

public @interface ForeignKey {

    String name();

    String referenceJpaSpec() default "";

    String referenceColumn();

    String referenceTable();

    String referenceChangeLogId() default "";

    String version() default "";
}
