package com.olsonsolution.common.spring.application.annotation.processor.migration;

record CreateSequence(String table, int startValue, int incrementBy) implements ChangeSetOperation {
}
