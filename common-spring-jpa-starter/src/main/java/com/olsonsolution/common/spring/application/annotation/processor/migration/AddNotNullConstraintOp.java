package com.olsonsolution.common.spring.application.annotation.processor.migration;

record AddNotNullConstraintOp(String table, String column) implements ChangeSetOperation{
}
