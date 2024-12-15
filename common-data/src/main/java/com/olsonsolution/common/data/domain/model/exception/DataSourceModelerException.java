package com.olsonsolution.common.data.domain.model.exception;

public class DataSourceModelerException extends RuntimeException {

    public DataSourceModelerException(String message) {
        super(message);
    }

    public DataSourceModelerException(String message, Throwable cause) {
        super(message, cause);
    }
}
