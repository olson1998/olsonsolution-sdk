package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@Builder(access = AccessLevel.PACKAGE, builderClassName = "Builder")
record ChangeOp(@NonNull String operation,
                @JsonInclude(NON_EMPTY) @Singular("attribute") @NonNull Map<String, String> attributes,
                @JsonInclude(NON_EMPTY) @Singular("childOperation") List<ChangeOp> childOperations) {
}
