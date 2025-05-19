package com.olsonsolution.common.spring.application.annotation.migration;

public @interface ChangeSet {

    String firstVersion() default "1.0.0";

    String path() default "src/resources/db/changelog/";

    String id() default "db.changelog-{table}-{version}";

    ColumnChange[] changes() default {};

    String[] columnOrder() default {};

    String[] versionChronology() default {};

    DependsOn[] dependsOn() default {};

    @interface DependsOn {

        String jpaSpec() default "";

        String table();

        String version() default "";

    }

}
