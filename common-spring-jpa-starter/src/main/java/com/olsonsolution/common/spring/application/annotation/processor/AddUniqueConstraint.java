package com.olsonsolution.common.spring.application.annotation.processor;

record AddUniqueConstraint(String table, String column, String name) implements ChangeSetOperation{
}
