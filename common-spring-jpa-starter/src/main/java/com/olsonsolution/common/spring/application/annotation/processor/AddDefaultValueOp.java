package com.olsonsolution.common.spring.application.annotation.processor;

record AddDefaultValueOp(String table, String column, String defaultValue) implements ChangeSetOperation {
}
