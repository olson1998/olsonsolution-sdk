package com.olsonsolution.common.spring.application.datasource.migration.annotation.processor;

record CreateSequence(String table, int startValue, int incrementBy) implements ChangeSetOperation {
}
