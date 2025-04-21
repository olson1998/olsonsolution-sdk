package com.olsonsolution.common.spring.application.datasource.migration.annotation;

import static com.olsonsolution.common.spring.application.datasource.migration.annotation.Operation.ADD;

public @interface ColumnChange {

    String column() default "";

    Operation operation() default ADD;

    String parameters() default "";

    String version();

}
