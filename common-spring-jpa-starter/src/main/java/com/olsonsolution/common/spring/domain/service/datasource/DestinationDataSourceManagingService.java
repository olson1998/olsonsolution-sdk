package com.olsonsolution.common.spring.domain.service.datasource;

import com.github.benmanes.caffeine.cache.Cache;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DestinationDataSourceManagingService implements DestinationDataSourceManager {

    private final DataSourceSpec initialDataSourceSpec;

    private final SqlDataSource initialSqlDataSource;

    private final DestinationDataSourceProvider destinationDataSourceProvider;

    private final Cache<String, SqlDataSource> sqlDataSourceCache;

    @Override
    public SqlDataSource obtainSqlDataSource(DataSourceSpec dataSourceSpec) {
        if(initialDataSourceSpec == dataSourceSpec) {
            return initialSqlDataSource;
        } else {
            return sqlDataSourceCache.get(dataSourceSpec.getName(), this::getSqlDataSource);
        }
    }

    private SqlDataSource getSqlDataSource(String dataSourceName) {
        SqlDataSource sqlDataSource = destinationDataSourceProvider.findDestination(dataSourceName)
                .orElseThrow();
        log.info("Routing data source name: '{}' Obtained", dataSourceName);
        return sqlDataSource;
    }

}
