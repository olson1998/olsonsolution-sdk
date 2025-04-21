package com.olsonsolution.common.spring.application.datasource.migration.annotation.processor;

import lombok.Builder;
import lombok.Singular;

import java.util.List;

@Builder
record AddColumnOp(String table,
                   String column,
                   @Singular("constraint") List<ConstraintMetadata> constraints) implements ChangeSetOperation {

}
