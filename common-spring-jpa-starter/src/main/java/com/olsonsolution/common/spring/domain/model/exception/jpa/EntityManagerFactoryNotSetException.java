package com.olsonsolution.common.spring.domain.model.exception.jpa;

public class EntityManagerFactoryNotSetException extends RuntimeException {

    private static final String MSG = "Entity manager factory not configured for current thread";

    public EntityManagerFactoryNotSetException() {
        super(MSG);
    }
}
