package com.olsonsolution.common.spring.domain.port.repository.hibernate;

import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public interface JpaEnvironmentManager extends CurrentTenantIdentifierResolver<JpaEnvironment> {

    void setCurrent(JpaEnvironment jpaEnvironment);

    void clear();

}
