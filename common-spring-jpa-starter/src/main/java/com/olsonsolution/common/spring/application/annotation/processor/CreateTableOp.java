package com.olsonsolution.common.spring.application.annotation.processor;

import java.util.List;

record CreateTableOp(String table, List<AddColumnOp> addColumns) implements ChangeSetOperation {
}
