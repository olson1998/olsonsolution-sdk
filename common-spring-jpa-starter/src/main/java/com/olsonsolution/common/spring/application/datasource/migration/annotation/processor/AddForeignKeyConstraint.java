package com.olsonsolution.common.spring.application.datasource.migration.annotation.processor;

record AddForeignKeyConstraint(String table,
                               String column,
                               String constraintName,
                               String referencedTable,
                               String referencedColumn) implements ChangeSetOperation {
}
