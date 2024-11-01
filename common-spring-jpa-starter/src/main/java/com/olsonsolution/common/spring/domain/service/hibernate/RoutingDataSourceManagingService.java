package com.olsonsolution.common.spring.domain.service.hibernate;

import com.github.benmanes.caffeine.cache.Cache;
import com.olsonsolution.common.spring.domain.model.exception.datasource.DestinationDataSourceNotFoundException;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.hibernate.RoutingDataSourceManager;
import com.olsonsolution.common.spring.domain.port.stereotype.hibernate.DataBaseEnvironment;
import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;

@RequiredArgsConstructor
public class RoutingDataSourceManagingService extends RoutingDataSourceManager {

    private static final String UNIT_NAME = "connection_pool_%s.%s";

    private final DestinationDataSourceProvider destinationDataSourceProvider;

    private final Cache<DataBaseEnvironment, HikariDataSource> destinationDataSourcesCache;

    @Override
    protected DataSource selectAnyDataSource() {
        DataBaseEnvironment dataBaseEnvironment = destinationDataSourceProvider.getProductDataSourceEnvironment();
        return createDestinationDataSource(dataBaseEnvironment);
    }

    @Override
    protected DataSource selectDataSource(JpaEnvironment tenantIdentifier) {
        DataBaseEnvironment dataBaseEnvironment = tenantIdentifier.getDataBaseEnvironment();
        return destinationDataSourcesCache.get(dataBaseEnvironment, this::createDestinationDataSource);
    }

    private HikariDataSource createDestinationDataSource(DataBaseEnvironment dataBaseEnvironment) {
        HikariConfig hikariConfig = destinationDataSourceProvider.findDestinationConfig(dataBaseEnvironment)
                .orElseThrow(() -> new DestinationDataSourceNotFoundException(dataBaseEnvironment));
        String unitName = UNIT_NAME.formatted(dataBaseEnvironment.getDataBase(), dataBaseEnvironment.getSchema());
        hikariConfig.setPoolName(unitName);
        return new HikariDataSource(hikariConfig);
    }

}
