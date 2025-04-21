package com.olsonsolution.common.spring.application.annotation.processor;

record AddNotNullConstraintOp(String table, String column) implements ChangeSetOperation{
}
