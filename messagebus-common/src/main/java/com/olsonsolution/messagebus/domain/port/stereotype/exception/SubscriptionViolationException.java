package com.olsonsolution.messagebus.domain.port.stereotype.exception;

public abstract class SubscriptionViolationException extends Exception {

    protected SubscriptionViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    protected SubscriptionViolationException(String message) {
        super(message);
    }
}
