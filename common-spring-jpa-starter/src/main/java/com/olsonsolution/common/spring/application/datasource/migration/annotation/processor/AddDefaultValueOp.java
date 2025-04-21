package com.olsonsolution.common.spring.application.datasource.migration.annotation.processor;

record AddDefaultValueOp(String table, String column, String defaultValue) implements ChangeSetOperation {
}
