package com.olsonsolution.common.spring.application.annotation.migration;

public @interface ColumnChanges {

    ColumnChange[] value() default {};

}
