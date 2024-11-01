package com.olsonsolution.common.spring.domain.port.repository.jpa;

import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;

public interface JpaEnvironmentConfigurable<D> {

    D getDelegate();

    void setEnvironment(JpaEnvironment jpaEnvironment);

    void unregisterDelegate(JpaEnvironment jpaEnvironment) throws Exception;

    void clear();

}
