package com.olsonsolution.common.spring.application.datasource.migration.annotation.processor;

record AddNotNullConstraintOp(String table, String column) implements ChangeSetOperation{
}
