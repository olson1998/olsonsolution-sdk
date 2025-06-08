package com.olsonsolution.common.spring.domain.service.datasource;

import com.github.benmanes.caffeine.cache.Cache;
import com.olsonsolution.common.data.domain.port.repository.sql.SqlDataSourceFactory;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.spring.domain.model.datasource.DomainJpaSpecDataSource;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.JpaDataSourceSpec;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

@Slf4j
@RequiredArgsConstructor
public class RoutingDataSourceManagingService extends DestinationDataSourceManager {

    private final JpaDataSourceSpec defaultDataSourceSpecification;

    private final SqlDataSourceFactory sqlDataSourceFactory;

    private final SqlDataSourceProvider sqlDataSourceProvider;

    private final Cache<JpaDataSourceSpec, DataSource> destinationDataSourcesCache;

    @Override
    public DataSource selectDataSourceBySpec(JpaDataSourceSpec jpaDataSourceSpec) {
        DomainJpaSpecDataSource domainJpaSpecDataSource = new DomainJpaSpecDataSource(
                jpaDataSourceSpec.getJpaSpec(), jpaDataSourceSpec.getDataSourceName(), jpaDataSourceSpec.getPermission()
        );
        return destinationDataSourcesCache.get(domainJpaSpecDataSource, this::createPermissionManagingDataSource);
    }

    @Override
    protected DataSource selectAnyDataSource() {
        return selectDataSource(defaultDataSourceSpecification);
    }

    @Override
    protected DataSource selectDataSource(JpaDataSourceSpec tenantIdentifier) {
        return selectDataSourceBySpec(tenantIdentifier);
    }

    private DataSource createPermissionManagingDataSource(@NonNull JpaDataSourceSpec jpaDataSourceSpec) {
        SqlPermission permission = jpaDataSourceSpec.getPermission();
        SqlDataSource sqlDataSource = sqlDataSourceProvider.findDestination(jpaDataSourceSpec).orElse(null);
        DataSource dataSource = sqlDataSourceFactory.fabricate(sqlDataSource, permission);
        log.info("Data Source {} provisioned for: {}", dataSource.getClass().getSimpleName(), jpaDataSourceSpec);
        return dataSource;
    }

}
