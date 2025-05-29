package com.olsonsolution.common.spring.application.annotation.migration;

public @interface ColumnChange {

    String column() default "";

    Operation op();

    Param[] params() default {};

    String version();

}
