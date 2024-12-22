package com.olsonsolution.common.spring.application.jpa.config;

import com.olsonsolution.common.spring.domain.port.props.jpa.EntityManagerFactoryProperties;
import com.olsonsolution.common.spring.domain.port.props.jpa.JpaProperties;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.hibernate.RoutingDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.EntityManagerFactoryDelegate;
import com.olsonsolution.common.spring.domain.port.repository.jpa.PlatformTransactionManagerDelegate;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import com.olsonsolution.common.spring.domain.service.jpa.MultiVendorPlatformTransactionManager;
import com.olsonsolution.common.spring.domain.service.jpa.MultiVendorRoutingEntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.JpaRepositoryConfigExtension;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationDelegate;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RoutingJpaConfigurer implements InitializingBean, ApplicationContextAware {

    public static final String LOG_MSG = "Jpa configured for schema: '{}'\n" +
            "Entity manager factory: '{}' instance: '{}'\n" +
            "Platform transaction manager: '{}' instance: '{}'\n" +
            "Entities base packages: {}";

    private ConfigurableApplicationContext applicationContext;

    private final JpaProperties jpaProperties;

    private final ResourceLoader resourceLoader;

    private final DataSourceSpecManager dataSourceSpecManager;

    private final DestinationDataSourceManager destinationDataSourceManager;

    private final RoutingDataSourceManager routingDataSourceManager;

    private final CurrentTenantIdentifierResolver<DataSourceSpec> dataSourceSpecResolver;

    private final Map<String, EntityManagerFactoryDelegate> entityManagerFactories = new HashMap<>();

    private final Map<String, PlatformTransactionManagerDelegate> platformTransactionManagers = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        for (EntityManagerFactoryProperties properties : jpaProperties.getEntityManagerFactoryProperties()) {
            registerJpaBeansDelegates(properties, beanFactory);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext instanceof ConfigurableApplicationContext context) {
            this.applicationContext = context;
        }
    }

    private void registerJpaBeansDelegates(EntityManagerFactoryProperties properties,
                                           ConfigurableListableBeanFactory beanFactory) {
        String schema = properties.getSchema();
        Set<String> entityPackagesToScan = properties.getEntityProperties().getPackagesToScan();
        Set<String> jpaRepoPackagesToScan = properties.getJpaRepositoryProperties().getPackagesToScan();
        String entityMangerFactoryBean = schema + "_entityManagerFactory";
        String platformTransactionManagerBean = schema + "_platformTransactionManager";
        EntityManagerFactoryDelegate entityManagerFactoryDelegate = new MultiVendorRoutingEntityManagerFactory(
                schema,
                jpaProperties,
                dataSourceSpecManager,
                destinationDataSourceManager,
                routingDataSourceManager,
                dataSourceSpecResolver
        );
        PlatformTransactionManagerDelegate platformTransactionManagerDelegate =
                new MultiVendorPlatformTransactionManager(
                        dataSourceSpecManager,
                        destinationDataSourceManager,
                        entityManagerFactoryDelegate
                );
        beanFactory.registerSingleton(entityMangerFactoryBean, entityManagerFactoryDelegate);
        beanFactory.registerSingleton(platformTransactionManagerBean, platformTransactionManagerDelegate);
        log.info(
                LOG_MSG,
                schema,
                entityMangerFactoryBean,
                entityManagerFactoryDelegate,
                platformTransactionManagerBean,
                platformTransactionManagerDelegate,
                entityPackagesToScan
        );
        entityManagerFactories.put(schema, entityManagerFactoryDelegate);
        platformTransactionManagers.put(schema, platformTransactionManagerDelegate);
        enableJpaRepositories(schema, entityMangerFactoryBean, platformTransactionManagerBean, jpaRepoPackagesToScan);
    }

    private void enableJpaRepositories(String schema,
                                       String entityManagerFactoryRef,
                                       String platformTransactionManagerRef,
                                       Set<String> jpaRepoPackagesToScan) {
        AnnotationMetadata enableJpaRepo = new EnableJpaRepositoriesMetadata(
                entityManagerFactoryRef,
                platformTransactionManagerRef,
                jpaRepoPackagesToScan
        );
        AutowireCapableBeanFactory autowireCapableBeanFactory = applicationContext.getAutowireCapableBeanFactory();
        BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) autowireCapableBeanFactory;
        Environment environment = applicationContext.getEnvironment();
        AnnotationRepositoryConfigurationSource configurationSource = new AnnotationRepositoryConfigurationSource(
                enableJpaRepo,
                EnableJpaRepositories.class,
                resourceLoader,
                environment,
                beanDefinitionRegistry,
                new AnnotationBeanNameGenerator()
        );
        RepositoryConfigurationExtension extension = new JpaRepositoryConfigExtension();
        RepositoryConfigurationDelegate configurationDelegate = new RepositoryConfigurationDelegate(
                configurationSource,
                resourceLoader,
                environment
        );
        configurationDelegate.registerRepositoriesIn(beanDefinitionRegistry, extension);
        log.info("Enabled jpa repositories: schema: '{}' base packages: {}", schema, jpaRepoPackagesToScan);
    }

}
