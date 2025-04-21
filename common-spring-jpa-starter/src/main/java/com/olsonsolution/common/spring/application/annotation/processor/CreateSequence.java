package com.olsonsolution.common.spring.application.annotation.processor;

record CreateSequence(String table, int startValue, int incrementBy) implements ChangeSetOperation {
}
