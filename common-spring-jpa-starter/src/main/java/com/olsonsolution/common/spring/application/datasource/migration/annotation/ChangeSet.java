package com.olsonsolution.common.spring.application.datasource.migration.annotation;

public @interface ChangeSet {

    Version version();

    @interface Version {

        int majorVersion() default 1;

        int minorVersion() default 0;

    }

}
