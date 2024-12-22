package com.olsonsolution.common.spring.domain.port.props.jpa;

import java.util.Collection;
import java.util.Map;

public interface JpaProperties {

    Map<String, String> getDataSourceModelersEnableProperties();

    DefaultDataSourceSpecProperties getDefaultDataSourceProperties();

    RoutingDataSourceProperties getRoutingDataSourceProperties();

    Collection<? extends EntityManagerFactoryProperties> getEntityManagerFactoryProperties();

}
