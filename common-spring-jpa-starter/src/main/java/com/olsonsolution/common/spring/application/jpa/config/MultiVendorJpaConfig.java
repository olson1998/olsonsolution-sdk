package com.olsonsolution.common.spring.application.jpa.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.olsonsolution.common.caching.application.props.DefaultCachingProperties;
import com.olsonsolution.common.caching.domain.port.props.CachingProperties;
import com.olsonsolution.common.caching.domain.port.repository.InMemoryCacheFactory;
import com.olsonsolution.common.spring.configurer.application.props.DefaultBeanDefinition;
import com.olsonsolution.common.spring.domain.port.props.jpa.JpaProperties;
import com.olsonsolution.common.spring.domain.port.props.jpa.RoutingDataSourceProperties;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.datasource.RoutingDataSourceEvictor;
import com.olsonsolution.common.spring.domain.port.repository.jpa.*;
import com.olsonsolution.common.spring.domain.port.repository.hibernate.RoutingDataSourceManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.RoutingDataSource;
import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;
import com.olsonsolution.common.spring.domain.service.datasource.RoutingDataSourceEvictionService;
import com.olsonsolution.common.spring.domain.service.hibernate.JpaEnvironmentIdentifierResolver;
import com.olsonsolution.common.spring.domain.service.jpa.*;
import com.olsonsolution.common.spring.domain.service.hibernate.RoutingDataSourceManagingService;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
public class MultiVendorJpaConfig {

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

    @Bean(ROUTING_DATA_SOURCE_EVICTOR_BEAN)
    public RoutingDataSourceEvictor routingDataSourceEvictor() {
        return new RoutingDataSourceEvictionService();
    }

    @Bean
    public JpaEnvironmentManager jpaEnvironmentManager() {
        return new JpaEnvironmentManagingService();
    }

    @Bean
    public CurrentTenantIdentifierResolver<JpaEnvironment> jpaEnvironmentCurrentTenantIdentifierResolver(JpaEnvironmentManager jpaEnvironmentManager) {
        return new JpaEnvironmentIdentifierResolver(jpaEnvironmentManager);
    }

    @Bean
    public RoutingDataSourceManager routingDataSourceManager(JpaProperties jpaProperties,
                                                             InMemoryCacheFactory inMemoryCacheFactory,
                                                             DestinationDataSourceProvider destinationDataSourceProvider) {
        RoutingDataSourceProperties routingDataSourceProperties = jpaProperties.getRoutingDataSourceProperties();
        int maximumDataSources = routingDataSourceProperties.getMaxDataSources();
        CachingProperties cachingProperties = new DefaultCachingProperties(
                maximumDataSources,
                maximumDataSources,
                null,
                routingDataSourceProperties.getExpireTimeout(),
                null,
                new DefaultBeanDefinition(Executor.class, ROUTING_DATA_SOURCE_EXECUTOR_BEAN),
                new DefaultBeanDefinition(RoutingDataSourceEvictor.class, ROUTING_DATA_SOURCE_EVICTOR_BEAN),
                null
        );
        Cache<RoutingDataSource, HikariDataSource> dataBaseEnvironmentHikariDataSourceCache =
                inMemoryCacheFactory.fabricate(cachingProperties);
        return new RoutingDataSourceManagingService(
                destinationDataSourceProvider,
                dataBaseEnvironmentHikariDataSourceCache
        );
    }

    @Bean
    public RoutingEntityManagerFactory routingEntityManagerFactory(JpaProperties jpaProperties,
                                                                   RoutingDataSourceManager routingDataSourceManager,
                                                                   CurrentTenantIdentifierResolver<JpaEnvironment> tenantIdentifierResolver) {
        return new MultiVendorRoutingEntityManagerFactory(
                jpaProperties,
                tenantIdentifierResolver,
                routingDataSourceManager
        );
    }

    @Bean
    public RoutingEntityManager routingEntityManager() {
        return new MultiVendorEntityManager();
    }

    @Bean
    public RoutingPlatformTransactionManager routingPlatformTransactionManager(RoutingEntityManagerFactory routingEntityManagerFactory) {
        return new MultiVendorPlatformTransactionManager(routingEntityManagerFactory);
    }

    @Bean
    public JpaEnvironmentConfigurer jpaEnvironmentConfigurer(RoutingEntityManager routingEntityManager,
                                                             RoutingEntityManagerFactory routingEntityManagerFactory,
                                                             RoutingPlatformTransactionManager routingPlatformTransactionManager) {
        return new JpaEnvironmentConfiguringService(
                routingEntityManager,
                routingEntityManagerFactory,
                routingPlatformTransactionManager
        );
    }

}
