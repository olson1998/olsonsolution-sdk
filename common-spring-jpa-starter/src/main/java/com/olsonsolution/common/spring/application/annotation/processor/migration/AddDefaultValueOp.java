package com.olsonsolution.common.spring.application.annotation.processor.migration;

record AddDefaultValueOp(String table, String column, String defaultValue) implements ChangeSetOperation {
}
