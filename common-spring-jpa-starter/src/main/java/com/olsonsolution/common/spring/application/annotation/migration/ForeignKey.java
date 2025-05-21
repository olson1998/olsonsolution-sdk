package com.olsonsolution.common.spring.application.annotation.migration;

public @interface ForeignKey {

    String name() default "";

    String referenceJpaSpec() default "";

    String referenceColumn();

    String referenceTable();

    String referenceChangeLogId() default "";

    String version() default "";
}
