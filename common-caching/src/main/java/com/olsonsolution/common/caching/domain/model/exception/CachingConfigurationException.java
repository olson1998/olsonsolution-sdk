package com.olsonsolution.common.caching.domain.model.exception;

public class CachingConfigurationException extends IllegalStateException {

    public CachingConfigurationException(String s) {
        super(s);
    }

    public CachingConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
