package com.olsonsolution.common.spring.domain.model.context.exception;

import com.olsonsolution.common.spring.domain.port.stereotype.context.LocalContext;

public class LocalContextNotInstanceException extends RuntimeException {

    private static final String MSG = "Local context expected to be %s but actually it is %s";

    private LocalContextNotInstanceException(String message) {
        super(message);
    }

    public static LocalContextNotInstanceException ofInstance(LocalContext localContext,
                                                              Class<? extends LocalContext> expectedContextClass) {
        String msg = MSG.formatted(expectedContextClass.getSimpleName(), localContext.getClass().getSimpleName());
        return new LocalContextNotInstanceException(msg);
    }

}
