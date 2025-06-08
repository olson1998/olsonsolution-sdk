package com.olsonsolution.common.spring.domain.port.props.jpa;

import java.util.Collection;
import java.util.Map;

public interface JpaProperties {

    String getDefaultDataSource();

    Map<String, String> getDataSourceModeler();

    RoutingDataSourceProperties getRoutingDataSource();

    Collection<? extends JpaSpecProperties> getJpaSpecConfig();

}
