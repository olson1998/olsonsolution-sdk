package com.olsonsolution.common.spring.domain.port.props.jpa;

import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;

import java.util.Collection;
import java.util.Map;

public interface JpaProperties {

    DataSourceSpec getDefaultDataSourceSpecProperties();

    Map<String, String> getDataSourceModelersEnableProperties();

    RoutingDataSourceProperties getRoutingDataSourceProperties();

    Collection<? extends EntityManagerFactoryProperties> getEntityManagerFactoryProperties();

}
