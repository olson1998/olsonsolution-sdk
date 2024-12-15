package com.olsonsolution.common.spring.application.jpa.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.olsonsolution.common.caching.application.props.DefaultCachingProperties;
import com.olsonsolution.common.caching.domain.port.props.CachingProperties;
import com.olsonsolution.common.caching.domain.port.repository.InMemoryCacheFactory;
import com.olsonsolution.common.data.domain.port.repository.sql.DataSourceModeler;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.service.sql.DataSourceModelingService;
import com.olsonsolution.common.spring.domain.port.props.jpa.JpaProperties;
import com.olsonsolution.common.spring.domain.port.props.jpa.RoutingDataSourceProperties;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DataSourceEvictor;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.hibernate.RoutingDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import com.olsonsolution.common.spring.domain.service.datasource.DataSourceEvictionService;
import com.olsonsolution.common.spring.domain.service.datasource.DestinationDataSourceManagingService;
import com.olsonsolution.common.spring.domain.service.hibernate.DataSourceSpecIdentifierResolver;
import com.olsonsolution.common.spring.domain.service.hibernate.RoutingDataSourceManagingService;
import com.olsonsolution.common.spring.domain.service.jpa.MultiVendorManagingService;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
public class DataSourceSpecConfig {

    public static final String ROUTING_DATA_SOURCE_EXECUTOR_BEAN = "routingDataSourceExecutor";
    public static final String ROUTING_DATA_SOURCE_EVICTOR_BEAN = "routingDataSourceEvictor";

    @Bean(ROUTING_DATA_SOURCE_EXECUTOR_BEAN)
    public Executor executor(JpaProperties jpaProperties) {
        ThreadFactory threadFactory = new BasicThreadFactory.Builder()
                .namingPattern("routing-data-source-%s")
                .build();
        return Executors.newFixedThreadPool(
                jpaProperties.getRoutingDataSourceProperties().getMaxDataSources(),
                threadFactory
        );
    }

    @Bean
    public DataSourceModeler dataSourceModeler() {
        return new DataSourceModelingService();
    }

    @Bean(ROUTING_DATA_SOURCE_EVICTOR_BEAN)
    public DataSourceEvictor routingDataSourceEvictor() {
        return new DataSourceEvictionService();
    }

    @Bean
    public DataSourceSpecManager jpaEnvironmentManager() {
        return new MultiVendorManagingService();
    }

    @Bean
    public CurrentTenantIdentifierResolver<DataSourceSpec> jpaEnvironmentCurrentTenantIdentifierResolver(DataSourceSpecManager dataSourceSpecManager) {
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
                maximumDataSources,
                null,
                routingDataSourceProperties.getExpireTimeout(),
                null
        );
        Cache<String, SqlDataSource> sqlDataSourceCache =
                inMemoryCacheFactory.fabricate(cachingProperties, null, null, null);
        return new DestinationDataSourceManagingService(destinationDataSourceProvider, sqlDataSourceCache);
    }

    @Bean
    public RoutingDataSourceManager routingDataSourceManager(JpaProperties jpaProperties,
                                                             InMemoryCacheFactory inMemoryCacheFactory,
                                                             DataSourceModeler dataSourceModeler,
                                                             DataSourceEvictor dataSourceEvictor,
                                                             DestinationDataSourceManager destinationDataSourceManager) {
        RoutingDataSourceProperties routingDataSourceProperties = jpaProperties.getRoutingDataSourceProperties();
        int maximumDataSources = routingDataSourceProperties.getMaxDataSources();
        CachingProperties cachingProperties = new DefaultCachingProperties(
                maximumDataSources,
                maximumDataSources,
                null,
                routingDataSourceProperties.getExpireTimeout(),
                null
        );
        Cache<DataSourceSpec, DataSource> destinationDataSourceCache =
                inMemoryCacheFactory.fabricate(cachingProperties, null, null, dataSourceEvictor);
        return new RoutingDataSourceManagingService(
                jpaProperties.getDefaultDataSourceProperties().getSpecProperties(),
                dataSourceModeler,
                destinationDataSourceManager,
                destinationDataSourceCache
        );
    }

}
