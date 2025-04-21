package com.olsonsolution.common.spring.application.annotation.migration;

public @interface ColumnChange {

    String column() default "";

    Operation operation();

    String parameters() default "";

    String version();

}
