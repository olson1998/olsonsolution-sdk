package com.olsonsolution.common.spring.domain.port.props.jpa;

import java.util.Collection;
import java.util.Map;

public interface JpaProperties {

    Map<String, String> getDataSourceModelersEnableProperties();

    RoutingDataSourceProperties getRoutingDataSourceProperties();

    Collection<? extends JpaSpecProperties> getJpaSpecificationsProperties();

}
