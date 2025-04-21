package com.olsonsolution.common.spring.application.annotation.processor;

record AddForeignKeyConstraint(String table,
                               String column,
                               String constraintName,
                               String referencedTable,
                               String referencedColumn) implements ChangeSetOperation {
}
