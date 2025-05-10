package com.olsonsolution.common.spring.application.annotation.processor.migration;

import java.util.List;

record CreateTableOp(String schemaVariable, String table, List<AddColumnOp> addColumns) implements ChangeSetOperation {
}
