package com.olsonsolution.common.spring.application.jpa.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.olsonsolution.common.caching.application.props.DefaultCachingProperties;
import com.olsonsolution.common.caching.domain.port.props.CachingProperties;
import com.olsonsolution.common.caching.domain.port.repository.InMemoryCacheFactory;
import com.olsonsolution.common.data.domain.port.repository.sql.DataSourceFactory;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.spring.application.jpa.props.SpringApplicationDestinationDataSourceProperties;
import com.olsonsolution.common.spring.domain.model.datasource.DataSourceSpecification;
import com.olsonsolution.common.spring.domain.port.props.jpa.JpaProperties;
import com.olsonsolution.common.spring.domain.port.props.jpa.RoutingDataSourceProperties;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DataSourceEvictor;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.datasource.RoutingDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import com.olsonsolution.common.spring.domain.service.datasource.DataSourceEvictionService;
import com.olsonsolution.common.spring.domain.service.datasource.DestinationDataSourceManagingService;
import com.olsonsolution.common.spring.domain.service.hibernate.DataSourceSpecIdentifierResolver;
import com.olsonsolution.common.spring.domain.service.datasource.RoutingDataSourceManagingService;
import com.olsonsolution.common.spring.domain.service.datasource.DataSourceSpecificationManagingService;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static com.olsonsolution.common.data.domain.model.sql.SqlPermissions.RWX;
import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.H2;

@Configuration
public class DataSourceSpecConfig {

    public static final String ROUTING_DATA_SOURCE_EVICTOR_BEAN = "routingDataSourceEvictor";

    private static final String H2_INITIAL_DATA_SOURCE_NAME = "H2_INITIAL";

    private static final DataSourceSpec H2_INITIAL_DATA_SOURCE_SPEC = DataSourceSpecification.builder()
            .name(H2_INITIAL_DATA_SOURCE_NAME)
            .permissions(RWX)
            .build();

    private static final SqlDataSource H2_INITIAL_DATA_SOURCE = initialDataSource();

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
    public CurrentTenantIdentifierResolver<DataSourceSpec> jpaEnvironmentCurrentTenantIdentifierResolver(
            DataSourceSpecManager dataSourceSpecManager) {
        return new DataSourceSpecIdentifierResolver(dataSourceSpecManager);

    }

    @Bean
    public DestinationDataSourceManager destinationDataSourceManager(JpaProperties jpaProperties,
                                                                     InMemoryCacheFactory inMemoryCacheFactory,
                                                                     DestinationDataSourceProvider destinationDataSourceProvider) {
        RoutingDataSourceProperties routingDataSourceProperties = jpaProperties.getRoutingDataSourceProperties();
        int maximumDataSources = routingDataSourceProperties.getMaxDataSources();
        CachingProperties cachingProperties = new DefaultCachingProperties(
                maximumDataSources,
                (long) maximumDataSources,
                null,
                routingDataSourceProperties.getExpireTimeout(),
                null
        );
        Cache<String, SqlDataSource> sqlDataSourceCache =
                inMemoryCacheFactory.fabricate(cachingProperties, null, null, null);
        return new DestinationDataSourceManagingService(
                H2_INITIAL_DATA_SOURCE_SPEC,
                H2_INITIAL_DATA_SOURCE,
                destinationDataSourceProvider,
                sqlDataSourceCache
        );
    }

    @Bean
    public RoutingDataSourceManager routingDataSourceManager(JpaProperties jpaProperties,
                                                             InMemoryCacheFactory inMemoryCacheFactory,
                                                             DataSourceFactory dataSourceFactory,
                                                             DataSourceEvictor dataSourceEvictor,
                                                             DestinationDataSourceManager destinationDataSourceManager) {
        RoutingDataSourceProperties routingDataSourceProperties = jpaProperties.getRoutingDataSourceProperties();
        int maximumDataSources = routingDataSourceProperties.getMaxDataSources();
        CachingProperties cachingProperties = new DefaultCachingProperties(
                maximumDataSources,
                (long) maximumDataSources,
                null,
                routingDataSourceProperties.getExpireTimeout(),
                null
        );
        Cache<DataSourceSpec, DataSource> destinationDataSourceCache =
                inMemoryCacheFactory.fabricate(cachingProperties, null, null, dataSourceEvictor);
        return new RoutingDataSourceManagingService(
                H2_INITIAL_DATA_SOURCE_SPEC,
                dataSourceFactory,
                destinationDataSourceManager,
                destinationDataSourceCache
        );
    }

    private static SqlDataSource initialDataSource() {
        var dataSource = new SpringApplicationDestinationDataSourceProperties.DataSourceProperties(
                H2,
                "mem",
                null,
                "INITIAL_DB"
        );
        SpringApplicationDestinationDataSourceProperties.UserProperties user =
                new SpringApplicationDestinationDataSourceProperties.UserProperties(
                "user",
                "pass"
        );
        dataSource.getUser().getRwx().add(user);
        return dataSource;
    }

}
