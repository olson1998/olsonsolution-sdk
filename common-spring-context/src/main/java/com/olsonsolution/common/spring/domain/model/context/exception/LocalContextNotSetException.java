package com.olsonsolution.common.spring.domain.model.context.exception;

public class LocalContextNotSetException extends RuntimeException {

    private static final String MSG = "Local context not set";

    public LocalContextNotSetException() {
        super(MSG);
    }
}
