package com.olsonsolution.common.spring.application.annotation.processor;

import lombok.Builder;
import lombok.Singular;

import java.util.List;

@Builder
record AddColumnOp(String column, String type, @Singular("constraint") List<ConstraintMetadata> constraints) {

}
