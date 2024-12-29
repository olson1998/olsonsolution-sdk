package com.olsonsolution.common.spring.application.jpa.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.olsonsolution.common.caching.application.props.DefaultCachingProperties;
import com.olsonsolution.common.caching.domain.port.props.CachingProperties;
import com.olsonsolution.common.caching.domain.port.repository.InMemoryCacheFactory;
import com.olsonsolution.common.data.domain.port.datasource.PermissionManagingDataSource;
import com.olsonsolution.common.data.domain.port.repository.sql.SqlDataSourceFactory;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.spring.application.jpa.props.ApplicationSqlDataSourceProperties;
import com.olsonsolution.common.spring.application.jpa.props.ApplicationSqlUserProperties;
import com.olsonsolution.common.spring.domain.model.datasource.DataSourceSpecification;
import com.olsonsolution.common.spring.domain.port.props.jpa.JpaProperties;
import com.olsonsolution.common.spring.domain.port.props.jpa.RoutingDataSourceProperties;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DataSourceEvictor;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import com.olsonsolution.common.spring.domain.service.datasource.DataSourceEvictionService;
import com.olsonsolution.common.spring.domain.service.datasource.DataSourceSpecificationManagingService;
import com.olsonsolution.common.spring.domain.service.datasource.RoutingDataSourceManagingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.olsonsolution.common.data.domain.model.sql.SqlPermissions.RWX;
import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.H2;

@Configuration
public class DataSourceSpecConfig {

    public static final String ROUTING_DATA_SOURCE_EVICTOR_BEAN = "routingDataSourceEvictor";

    public static final String H2_INITIAL_DATA_SOURCE_NAME = "H2_INITIAL";

    public static final SqlDataSource H2_INITIAL_DATA_SOURCE = initialDataSource();

    private static final DataSourceSpec H2_INITIAL_DATA_SOURCE_SPEC = DataSourceSpecification.builder()
            .name(H2_INITIAL_DATA_SOURCE_NAME)
            .permissions(RWX)
            .build();

    @Bean(ROUTING_DATA_SOURCE_EVICTOR_BEAN)
    public DataSourceEvictor routingDataSourceEvictor() {
        return new DataSourceEvictionService();
    }

    @Bean
    public DataSourceSpecManager jpaEnvironmentManager() {
        DataSourceSpecManager manager = new DataSourceSpecificationManagingService();
        manager.setThreadLocal(H2_INITIAL_DATA_SOURCE_SPEC);
        return manager;
    }

    @Bean
    public DestinationDataSourceManager routingDataSourceManager(JpaProperties jpaProperties,
                                                                 InMemoryCacheFactory inMemoryCacheFactory,
                                                                 SqlDataSourceFactory sqlDataSourceFactory,
                                                                 SqlDataSourceProvider sqlDataSourceProvider,
                                                                 DataSourceSpecManager dataSourceSpecManager,
                                                                 DataSourceEvictor dataSourceEvictor) {
        RoutingDataSourceProperties routingDataSourceProperties = jpaProperties.getRoutingDataSourceProperties();
        int maximumDataSources = routingDataSourceProperties.getMaxDataSources();
        CachingProperties cachingProperties = new DefaultCachingProperties(
                maximumDataSources,
                (long) maximumDataSources,
                null,
                routingDataSourceProperties.getExpireTimeout(),
                null
        );
        Cache<String, PermissionManagingDataSource> destinationDataSourceCache =
                inMemoryCacheFactory.fabricate(cachingProperties, null, null, dataSourceEvictor);
        return new RoutingDataSourceManagingService(
                H2_INITIAL_DATA_SOURCE_SPEC,
                sqlDataSourceFactory,
                sqlDataSourceProvider,
                dataSourceSpecManager,
                destinationDataSourceCache
        );
    }

    private static SqlDataSource initialDataSource() {
        var dataSource = new ApplicationSqlDataSourceProperties(
                H2,
                "mem",
                null,
                "INITIAL_DB"
        );
        ApplicationSqlUserProperties user = new ApplicationSqlUserProperties(
                "user",
                "pass"
        );
        dataSource.getUser().getRwx().add(user);
        return dataSource;
    }

}
