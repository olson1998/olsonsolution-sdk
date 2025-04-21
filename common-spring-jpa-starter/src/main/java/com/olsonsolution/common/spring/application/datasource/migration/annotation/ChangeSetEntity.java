package com.olsonsolution.common.spring.application.datasource.migration.annotation;

public @interface ChangeSetEntity {

    String path() default  "src/resources/db/changelog";

    String file() default  "db.changelog-{table}-{version}.xml";

    ColumnChanges changes();

}
