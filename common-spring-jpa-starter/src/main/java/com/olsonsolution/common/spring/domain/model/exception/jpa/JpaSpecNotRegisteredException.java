package com.olsonsolution.common.spring.domain.model.exception.jpa;

public class JpaSpecNotRegisteredException extends RuntimeException {

    private static final String MSG = "Jpa spec not registered for name: '%s'";

    private JpaSpecNotRegisteredException(String message) {
        super(message);
    }

    public static JpaSpecNotRegisteredException forName(String name) {
        return new JpaSpecNotRegisteredException(String.format(MSG, name));
    }

}
