package com.olsonsolution.common.spring.domain.port.repository.datasource;

import com.olsonsolution.common.spring.domain.port.stereotype.datasource.RoutingDataSource;
import com.zaxxer.hikari.HikariConfig;

import java.util.Optional;

public interface DestinationDataSourceProvider {

    RoutingDataSource getProductDataSourceEnvironment();

    Optional<HikariConfig> findDestinationConfig(RoutingDataSource routingDataSource);

}
