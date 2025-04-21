package com.olsonsolution.common.spring.application.datasource.migration.annotation.processor;

import java.util.List;

record CreateTableOp(String table, List<AddColumnOp> addColumns) implements ChangeSetOperation {
}
