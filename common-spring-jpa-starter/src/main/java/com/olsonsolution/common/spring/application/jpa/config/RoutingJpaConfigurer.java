package com.olsonsolution.common.spring.application.jpa.config;

import com.olsonsolution.common.spring.domain.port.props.jpa.EntityManagerFactoryProperties;
import com.olsonsolution.common.spring.domain.port.props.jpa.JpaProperties;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.hibernate.RoutingDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecConfigurer;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingEntityManagerFactory;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingPlatformTransactionManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import com.olsonsolution.common.spring.domain.service.jpa.MultiVendorPlatformTransactionManager;
import com.olsonsolution.common.spring.domain.service.jpa.MultiVendorRoutingEntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RoutingJpaConfigurer implements InitializingBean, ApplicationContextAware {

    public static final String LOG_MSG =
            """
                    Jpa configured for schema: '{}'
                    Entity manager factory: '{}' instance: '{}'
                    Platform transaction manager: '{}' instance: '{}'
                    Entities scanned packages: {}
                    Jpa repositories scanned packages: {}
                    """;

    private ConfigurableApplicationContext applicationContext;

    private final JpaProperties jpaProperties;

    private final DataSourceSpecManager dataSourceSpecManager;

    private final DestinationDataSourceManager destinationDataSourceManager;

    private final RoutingDataSourceManager routingDataSourceManager;

    private final CurrentTenantIdentifierResolver<DataSourceSpec> dataSourceSpecResolver;

    @Override
    public void afterPropertiesSet() throws Exception {
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        Map<String, RoutingEntityManagerFactory> routingEntityManagerFactories = new HashMap<>();
        Map<String, RoutingPlatformTransactionManager> routingPlatformTransactionManagers = new HashMap<>();
        for (EntityManagerFactoryProperties properties : jpaProperties.getEntityManagerFactoryProperties()) {
            String schema = properties.getSchema();
            Set<String> entityPackagesToScan = properties.getEntityProperties().getPackagesToScan();
            Set<String> jpaRepoPackagesToScan = properties.getJpaRepositoryProperties().getPackagesToScan();
            String entityMangerFactoryBean = schema + "_entityManagerFactory";
            String entityMangerBean = schema + "_entityManager";
            String platformTransactionManagerBean = schema + "_platformTransactionManager";
            RoutingEntityManagerFactory routingEntityManagerFactory = new MultiVendorRoutingEntityManagerFactory(
                    schema,
                    jpaProperties,
                    dataSourceSpecManager,
                    destinationDataSourceManager,
                    routingDataSourceManager,
                    dataSourceSpecResolver
            );
            RoutingPlatformTransactionManager routingPlatformTransactionManager =
                    new MultiVendorPlatformTransactionManager(
                            dataSourceSpecManager,
                            destinationDataSourceManager,
                            routingEntityManagerFactory
                    );
            beanFactory.registerSingleton(entityMangerFactoryBean, routingEntityManagerFactory);
            beanFactory.registerSingleton(entityMangerBean, routingPlatformTransactionManager);
            beanFactory.registerSingleton(platformTransactionManagerBean, routingPlatformTransactionManager);
            log.info(LOG_MSG, schema,
                    entityMangerFactoryBean, routingEntityManagerFactory,
                    platformTransactionManagerBean, routingPlatformTransactionManager,
                    entityPackagesToScan,
                    jpaRepoPackagesToScan
            );
            routingEntityManagerFactories.put(schema, routingEntityManagerFactory);
            routingPlatformTransactionManagers.put(schema, routingPlatformTransactionManager);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext instanceof ConfigurableApplicationContext context) {
            this.applicationContext = context;
        }
    }
}
