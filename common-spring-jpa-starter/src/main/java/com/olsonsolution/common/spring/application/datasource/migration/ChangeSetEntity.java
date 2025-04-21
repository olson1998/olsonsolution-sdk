package com.olsonsolution.common.spring.application.datasource.migration;

public @interface ChangeSetEntity {

    String path() default  "src/resources/db/changelog";

    String file() default  "db.changelog-{table}-{version}.xml";

    public @interface ColumnChange {

        String column();

        ChangeSet changeSet();

    }

}
