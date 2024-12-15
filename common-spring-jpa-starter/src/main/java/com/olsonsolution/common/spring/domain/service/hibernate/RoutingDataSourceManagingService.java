package com.olsonsolution.common.spring.domain.service.hibernate;

import com.github.benmanes.caffeine.cache.Cache;
import com.olsonsolution.common.data.domain.port.repository.sql.DataSourceModeler;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.hibernate.RoutingDataSourceManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

@Slf4j
@RequiredArgsConstructor
public class RoutingDataSourceManagingService extends RoutingDataSourceManager {

    private final DataSourceSpec defaultDataSourceSpec;

    private final DataSourceModeler dataSourceModeler;

    private final DestinationDataSourceManager destinationDataSourceManager;

    private final Cache<DataSourceSpec, DataSource> destinationDataSourcesCache;

    @Override
    protected DataSource selectAnyDataSource() {
        return selectDataSource(defaultDataSourceSpec);
    }

    @Override
    protected DataSource selectDataSource(DataSourceSpec tenantIdentifier) {
        return destinationDataSourcesCache.get(tenantIdentifier, this::createDataSource);
    }

    private DataSource createDataSource(DataSourceSpec dataSourceSpec) {
        String name = dataSourceSpec.getName();
        SqlDataSource sqlDataSource = destinationDataSourceManager.obtainSqlDataSource(name);
        DataSource dataSource = dataSourceModeler.createDataSource(sqlDataSource, dataSourceSpec.getPermission());
        log.info(
                "Routing data source spec: {} vendor: '{}' host: '{}'",
                dataSourceSpec,
                sqlDataSource.getVendor(),
                sqlDataSource.getHost()
        );
        return dataSource;
    }

}
