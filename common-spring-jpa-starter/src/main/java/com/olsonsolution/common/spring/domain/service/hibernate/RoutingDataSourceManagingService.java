package com.olsonsolution.common.spring.domain.service.hibernate;

import com.github.benmanes.caffeine.cache.Cache;
import com.olsonsolution.common.spring.domain.model.exception.datasource.DestinationDataSourceNotFoundException;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.hibernate.RoutingDataSourceManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.RoutingDataSource;
import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;

@RequiredArgsConstructor
public class RoutingDataSourceManagingService extends RoutingDataSourceManager {

    private static final String UNIT_NAME = "connection_pool_%s.%s";

    private final DestinationDataSourceProvider destinationDataSourceProvider;

    private final Cache<RoutingDataSource, HikariDataSource> destinationDataSourcesCache;

    @Override
    protected DataSource selectAnyDataSource() {
        RoutingDataSource routingDataSource = destinationDataSourceProvider.getProductDataSourceEnvironment();
        return createDestinationDataSource(routingDataSource);
    }

    @Override
    protected DataSource selectDataSource(JpaEnvironment tenantIdentifier) {
        RoutingDataSource routingDataSource = tenantIdentifier.getDataBaseEnvironment();
        return destinationDataSourcesCache.get(routingDataSource, this::createDestinationDataSource);
    }

    private HikariDataSource createDestinationDataSource(RoutingDataSource routingDataSource) {
        HikariConfig hikariConfig = destinationDataSourceProvider.findDestinationConfig(routingDataSource)
                .orElseThrow(() -> new DestinationDataSourceNotFoundException(routingDataSource));
        String unitName = UNIT_NAME.formatted(routingDataSource.getDataBase(), routingDataSource.getSchema());
        hikariConfig.setPoolName(unitName);
        return new HikariDataSource(hikariConfig);
    }

}
