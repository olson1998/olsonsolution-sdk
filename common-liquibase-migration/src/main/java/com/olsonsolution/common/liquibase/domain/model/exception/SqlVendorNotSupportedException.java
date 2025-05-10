package com.olsonsolution.common.liquibase.domain.model.exception;

import lombok.Getter;
import org.joda.time.MutableDateTime;

import javax.sql.DataSource;

public class SqlVendorNotSupportedException extends Exception {

    private static final String MESSAGE = "Sql vendor not supported for data source: %s";

    @Getter
    private final MutableDateTime startTimestamp;

    public SqlVendorNotSupportedException(DataSource dataSource, MutableDateTime startTimestamp) {
        super(String.format(MESSAGE, dataSource.getClass().getName()));
        this.startTimestamp = startTimestamp;
    }
}
