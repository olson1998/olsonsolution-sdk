package com.olsonsolution.common.spring.application.jpa.config;

import com.olsonsolution.common.spring.domain.port.props.jpa.JpaProperties;
import com.olsonsolution.common.spring.domain.port.props.jpa.JpaSpecProperties;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.EntityManagerFactoryDelegate;
import com.olsonsolution.common.spring.domain.port.repository.jpa.PlatformTransactionManagerDelegate;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import com.olsonsolution.common.spring.domain.service.jpa.MultiVendorEntityManagerFactory;
import com.olsonsolution.common.spring.domain.service.jpa.MultiVendorPlatformTransactionManager;
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

import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MultiVendorJpaConfigurer implements InitializingBean, ApplicationContextAware {

    public static final String LOG_MSG = "Jpa configured for schema: '{}'\n" +
            "Entity manager factory: '{}' instance: '{}'\n" +
            "Platform transaction manager: '{}' instance: '{}'\n" +
            "Entities base packages: {}";

    private ConfigurableApplicationContext applicationContext;

    private final JpaProperties jpaProperties;

    private final ResourceLoader resourceLoader;

    private final DataSourceSpecManager dataSourceSpecManager;

    private final SqlDataSourceProvider sqlDataSourceProvider;

    private final DestinationDataSourceManager destinationDataSourceManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        for (JpaSpecProperties properties : jpaProperties.getJpaSpecificationsProperties()) {
            registerJpaBeansDelegates(properties, beanFactory);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext instanceof ConfigurableApplicationContext context) {
            this.applicationContext = context;
        }
    }

    private void registerJpaBeansDelegates(JpaSpecProperties properties,
                                           ConfigurableListableBeanFactory beanFactory) {
        String schema = properties.getSchema();
        String name = properties.getName();
        Set<String> entityPackagesToScan = properties.getEntityProperties().getPackagesToScan();
        Set<String> jpaRepoPackagesToScan = properties.getJpaRepositoryProperties().getPackagesToScan();
        String entityMangerFactoryBean = name + "_entityManagerFactory";
        String platformTransactionManagerBean = name + "_platformTransactionManager";
        EntityManagerFactoryDelegate entityManagerFactoryDelegate = new MultiVendorEntityManagerFactory(
                schema,
                name,
                jpaProperties,
                dataSourceSpecManager,
                sqlDataSourceProvider,
                destinationDataSourceManager
        );
        PlatformTransactionManagerDelegate platformTransactionManagerDelegate =
                new MultiVendorPlatformTransactionManager(
                        dataSourceSpecManager,
                        sqlDataSourceProvider,
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
