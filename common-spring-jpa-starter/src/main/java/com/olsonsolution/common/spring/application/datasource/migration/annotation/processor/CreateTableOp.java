package com.olsonsolution.common.spring.application.datasource.migration.annotation.processor;

import lombok.Builder;
import lombok.Singular;

import java.util.List;

@Builder
record CreateTableOp(String table, @Singular("addColumn") List<AddColumnOp> addColumns)
        implements ChangeSetOperation {
}
