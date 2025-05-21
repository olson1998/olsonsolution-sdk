package com.olsonsolution.common.spring.application.annotation.migration;

public @interface ChangeSet {

    String id() default "db.changelog-{table}-{version}";

    ColumnChange[] changes() default {};

    DependsOn[] dependsOn() default {};

    @interface DependsOn {

        String jpaSpec() default "";

        String table();

        String version() default "";

    }

}
