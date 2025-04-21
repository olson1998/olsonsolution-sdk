package com.olsonsolution.common.spring.application.datasource.migration.annotation.processor;

record AddUniqueConstraint(String table, String column, String name) implements ChangeSetOperation{
}
