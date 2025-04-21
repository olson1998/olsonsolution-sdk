package com.olsonsolution.common.spring.application.datasource.migration.annotation;

public @interface ChangeSet {

    String path() default  "src/resources/db/changelog";

    String file() default  "db.changelog-{table}-{version}.xml";

    ColumnChange[] changes() default {};

    String[] columnOrder() default {};

}
