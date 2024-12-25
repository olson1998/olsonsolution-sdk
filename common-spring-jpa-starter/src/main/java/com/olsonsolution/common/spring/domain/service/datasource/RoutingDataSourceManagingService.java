package com.olsonsolution.common.spring.domain.service.datasource;

import com.github.benmanes.caffeine.cache.Cache;
import com.olsonsolution.common.data.domain.port.repository.sql.DataSourceFactory;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.RoutingDataSourceManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

@Slf4j
@RequiredArgsConstructor
public class RoutingDataSourceManagingService extends RoutingDataSourceManager {

    private final DataSourceSpec defaultDataSourceSpec;

    private final DataSourceFactory dataSourceFactory;

    private final DestinationDataSourceManager destinationDataSourceManager;

    private final Cache<DataSourceSpec, DataSource> destinationDataSourcesCache;

    @Override
    public DataSource selectDataSourceBySpec(DataSourceSpec dataSourceSpec) {
        return destinationDataSourcesCache.get(dataSourceSpec, this::createDataSource);
    }

    @Override
    protected DataSource selectAnyDataSource() {
        return selectDataSource(defaultDataSourceSpec);
    }

    @Override
    protected DataSource selectDataSource(DataSourceSpec tenantIdentifier) {
        return selectDataSourceBySpec(tenantIdentifier);
    }

    private DataSource createDataSource(DataSourceSpec dataSourceSpec) {
        SqlDataSource sqlDataSource = destinationDataSourceManager.obtainSqlDataSource(dataSourceSpec);
        DataSource dataSource = dataSourceFactory.fabricate(sqlDataSource, dataSourceSpec.getPermission());
        log.info(
                "Destination data source specification: {} host: '{}' database: '{}'",
                dataSourceSpec,
                sqlDataSource.getVendor(),
                sqlDataSource.getDatabase()
        );
        return dataSource;
    }

}
