package com.olsonsolution.common.spring.domain.service.datasource;

import com.github.benmanes.caffeine.cache.Cache;
import com.olsonsolution.common.data.domain.port.datasource.PermissionManagingDataSource;
import com.olsonsolution.common.data.domain.port.repository.sql.SqlDataSourceFactory;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.service.datasource.DomainPermissionManagingDataSource;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

@Slf4j
@RequiredArgsConstructor
public class RoutingDataSourceManagingService extends DestinationDataSourceManager {

    private final DataSourceSpecification defaultDataSourceSpecification;

    private final SqlDataSourceFactory sqlDataSourceFactory;

    private final SqlDataSourceProvider sqlDataSourceProvider;

    private final DataSourceSpecManager dataSourceSpecManager;

    private final Cache<String, PermissionManagingDataSource> destinationDataSourcesCache;

    @Override
    public DataSource selectDataSourceBySpec(DataSourceSpecification dataSourceSpecification) {
        return destinationDataSourcesCache.get(dataSourceSpecification.getName(), this::createPermissionManagingDataSource);
    }

    @Override
    protected DataSource selectAnyDataSource() {
        return selectDataSource(defaultDataSourceSpecification);
    }

    @Override
    protected DataSource selectDataSource(DataSourceSpecification tenantIdentifier) {
        return selectDataSourceBySpec(tenantIdentifier);
    }

    private PermissionManagingDataSource createPermissionManagingDataSource(String dataSourceName) {
        SqlDataSource sqlDataSource = sqlDataSourceProvider.findDestination(dataSourceName).orElse(null);
        PermissionManagingDataSource permissionManagingDataSource = new DomainPermissionManagingDataSource(
                sqlDataSource,
                sqlDataSourceFactory,
                dataSourceSpecManager
        );
        log.info("Permission data source manager created, name: '{}'", dataSourceName);
        return permissionManagingDataSource;
    }

}
