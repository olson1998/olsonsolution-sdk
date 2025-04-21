package com.olsonsolution.common.spring.application.annotation.migration;

public @interface ColumnChanges {

    ColumnChange[] atBeginning() default {};

    ColumnChange[] atEnd() default {};

}
