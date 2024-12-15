package com.olsonsolution.common.spring.domain.port.props.jpa;

import java.util.Collection;

public interface JpaProperties {

    DefaultDataSourceSpecProperties getDefaultDataSourceProperties();

    RoutingDataSourceProperties getRoutingDataSourceProperties();

    Collection<? extends EntityManagerFactoryProperties> getEntityManagerFactoryProperties();

}
