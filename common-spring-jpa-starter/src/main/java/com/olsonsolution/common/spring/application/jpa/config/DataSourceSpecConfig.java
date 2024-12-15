package com.olsonsolution.common.spring.application.jpa.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.olsonsolution.common.caching.application.props.DefaultCachingProperties;
import com.olsonsolution.common.caching.domain.port.props.CachingProperties;
import com.olsonsolution.common.caching.domain.port.repository.InMemoryCacheFactory;
import com.olsonsolution.common.data.domain.port.repository.sql.DataSourceModeler;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.service.sql.DataSourceModelingService;
import com.olsonsolution.common.spring.configurer.application.props.DefaultBeanDefinition;
import com.olsonsolution.common.spring.domain.port.props.jpa.JpaProperties;
import com.olsonsolution.common.spring.domain.port.props.jpa.RoutingDataSourceProperties;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DataSourceEvictor;
import com.olsonsolution.common.spring.domain.port.repository.jpa.*;
import com.olsonsolution.common.spring.domain.port.repository.hibernate.RoutingDataSourceManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.RoutingDataSource;
import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;
import com.olsonsolution.common.spring.domain.service.datasource.DataSourceEvictionService;
import com.olsonsolution.common.spring.domain.service.hibernate.DataSourceSpecIdentifierResolver;
import com.olsonsolution.common.spring.domain.service.jpa.*;
import com.olsonsolution.common.spring.domain.service.hibernate.RoutingDataSourceManagingService;
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
        return new DataSourceSpecificationManagingService();
    }

    @Bean
    public CurrentTenantIdentifierResolver<DataSourceSpec> jpaEnvironmentCurrentTenantIdentifierResolver(DataSourceSpecManager dataSourceSpecManager) {
        return new DataSourceSpecIdentifierResolver(dataSourceSpecManager);

    }

    @Bean
    public RoutingDataSourceManager routingDataSourceManager(DataSourceModeler dataSourceModeler,
                                                             InMemoryCacheFactory inMemoryCacheFactory,
                                                             DestinationDataSourceProvider destinationDataSourceProvider,
                                                             JpaProperties jpaProperties) {
        RoutingDataSourceProperties routingDataSourceProperties = jpaProperties.getRoutingDataSourceProperties();
        int maximumDataSources = routingDataSourceProperties.getMaxDataSources();
        CachingProperties cachingProperties = new DefaultCachingProperties(
                maximumDataSources,
                maximumDataSources,
                null,
                routingDataSourceProperties.getExpireTimeout(),
                null,
                new DefaultBeanDefinition(Executor.class, ROUTING_DATA_SOURCE_EXECUTOR_BEAN),
                new DefaultBeanDefinition(DataSourceEvictor.class, ROUTING_DATA_SOURCE_EVICTOR_BEAN),
                null
        );
        Cache<String, SqlDataSource> sqlDataSourceCache = inMemoryCacheFactory.fabricate(cachingProperties);
        Cache<DataSourceSpec, DataSource> destinationDataSourceCache =
                inMemoryCacheFactory.fabricate(cachingProperties);
        return new RoutingDataSourceManagingService(
                jpaProperties.getDefaultDataSourceProperties().getSpecProperties(),
                dataSourceModeler,
                destinationDataSourceProvider,
                sqlDataSourceCache,
                destinationDataSourceCache
        );
    }

}
