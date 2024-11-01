package com.olsonsolution.common.spring.domain.model.exception.jpa;

import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;

public class EntityManagerFactoryNotCratedException extends RuntimeException {

    private static final String MSG = "Entity manager factory nor created for dataSource: '%s' dialect: '%s'";

    public EntityManagerFactoryNotCratedException(JpaEnvironment jpaEnvironment) {
        super(MSG.formatted(jpaEnvironment.getDataSourceId(), jpaEnvironment.getDialect().getSimpleName()));
    }
}
