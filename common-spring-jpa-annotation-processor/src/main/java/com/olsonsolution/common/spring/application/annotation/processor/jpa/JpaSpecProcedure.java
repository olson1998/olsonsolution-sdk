package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@Builder(access = AccessLevel.PACKAGE, builderClassName = "Builder")
record JpaSpecProcedure(@NonNull JpaSpecMetadata metadata,
                        @JsonInclude(NON_EMPTY) @Singular("changeSet") List<ChangeSetOp> changeSets) {

}
