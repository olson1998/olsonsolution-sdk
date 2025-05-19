package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;

import javax.lang.model.element.TypeElement;
import java.util.List;

@Builder(access = AccessLevel.PACKAGE)
record EntityConfig(@NonNull String table, @NonNull TypeElement entity, @NonNull List<TypeElement> jpaRepositories) {

}
