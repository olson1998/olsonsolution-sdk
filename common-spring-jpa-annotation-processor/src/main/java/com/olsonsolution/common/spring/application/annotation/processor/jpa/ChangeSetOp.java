package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@Builder(access = AccessLevel.PACKAGE, builderClassName = "Builder")
record ChangeSetOp(@NonNull String id,
                   @NonNull String table,
                   @NonNull String version,
                   @NonNull List<ChangeOp> operations,
                   @JsonInclude(NON_EMPTY) @NonNull Map<String, Set<String>> dependsOn) {
}
