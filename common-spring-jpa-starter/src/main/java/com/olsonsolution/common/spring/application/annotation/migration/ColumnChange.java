package com.olsonsolution.common.spring.application.annotation.migration;

public @interface ColumnChange {

    String column() default "";

    Operation operation();

    Parameter[] parameters() default {};

    String version();

    @interface Parameter {

        String name();

        String value();

    }

}
