package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.processing.GenericDialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.hibernate.type.spi.TypeConfiguration;

@Getter
@RequiredArgsConstructor
class GenericJdbcTypeIndicators implements JdbcTypeIndicators {

    private final TypeConfiguration typeConfiguration;

    private final Dialect dialect = new GenericDialect();

}
