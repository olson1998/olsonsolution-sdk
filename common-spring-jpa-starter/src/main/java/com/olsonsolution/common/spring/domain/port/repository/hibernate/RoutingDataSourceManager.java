package com.olsonsolution.common.spring.domain.port.repository.hibernate;

import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;

public abstract class RoutingDataSourceManager extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl<JpaEnvironment> {

}
