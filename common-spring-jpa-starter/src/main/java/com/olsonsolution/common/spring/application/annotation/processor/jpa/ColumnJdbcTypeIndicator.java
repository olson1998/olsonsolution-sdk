package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.hibernate.type.spi.TypeConfiguration;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ColumnJdbcTypeIndicator implements JdbcTypeIndicators {


    private final int columnScale;

    private final int columnPrecision;

    private final long columnLength;

    private final Dialect dialect;

    private final TypeConfiguration typeConfiguration;

    public static ColumnJdbcTypeIndicator forAnnotation(Column column,
                                                        TypeConfiguration typeConfiguration) {
        return new ColumnJdbcTypeIndicator(
                column != null ? column.scale() : 0,
                column != null ? column.precision() : 0,
                column != null ? column.length() : 0,
                null,
                typeConfiguration
        );
    }

}
