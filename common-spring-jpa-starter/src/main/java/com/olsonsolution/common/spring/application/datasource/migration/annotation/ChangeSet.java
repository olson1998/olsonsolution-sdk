package com.olsonsolution.common.spring.application.datasource.migration.annotation;

public @interface ChangeSet {

    String firstVersion() default "1.0.0";

    String path() default  "src/resources/db/changelog";

    String file() default  "db.changelog-{table}-{version}.xml";

    ColumnChange[] changes() default {};

    String[] columnOrder() default {};

    String[] versionChronology() default {};

}
