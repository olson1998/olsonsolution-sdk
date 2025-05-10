package com.olsonsolution.common.spring.application.jpa.config;

import com.olsonsolution.common.spring.domain.port.props.jpa.JpaProperties;
import com.olsonsolution.common.spring.domain.port.props.jpa.JpaSpecProperties;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.EntityManagerFactoryDelegate;
import com.olsonsolution.common.spring.domain.port.repository.jpa.PlatformTransactionManagerDelegate;
import com.olsonsolution.common.spring.domain.service.jpa.MultiVendorEntityManagerFactory;
import com.olsonsolution.common.spring.domain.service.jpa.MultiVendorPlatformTransactionManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JpaSpecConfigurer {

    private final JpaProperties jpaProperties;

    public String resolveSchema(String jpaSpecName) {
        JpaSpecProperties properties = getJpaSpecProperties(jpaSpecName);
        return properties.getSchema();
    }

    public EntityManagerFactoryDelegate createEntityManagerFactoryDelegate(
            @NonNull String jpaSpecName,
            @NonNull String[] entityBasePackages,
            DataSourceSpecManager dataSourceSpecManager,
            SqlDataSourceProvider sqlDataSourceProvider,
            DestinationDataSourceManager destinationDataSourceManager) {
        JpaSpecProperties properties = getJpaSpecProperties(jpaSpecName);
        String schema = properties.getSchema();
        String name = properties.getName();
        log.info("Jpa Spec: '{}' configured for schema: '{}'", name, schema);
        return new MultiVendorEntityManagerFactory(
                schema,
                name,
                entityBasePackages,
                jpaProperties,
                dataSourceSpecManager,
                sqlDataSourceProvider,
                destinationDataSourceManager
        );
    }

    public PlatformTransactionManagerDelegate createPlatformTransactionManagerDelegate(
            DataSourceSpecManager dataSourceSpecManager,
            SqlDataSourceProvider sqlDataSourceProvider,
            EntityManagerFactoryDelegate entityManagerFactoryDelegate) {
        return new MultiVendorPlatformTransactionManager(
                dataSourceSpecManager,
                sqlDataSourceProvider,
                entityManagerFactoryDelegate
        );
    }

    private JpaSpecProperties getJpaSpecProperties(String jpaSpecName) {
        return jpaProperties.getJpaSpecificationsProperties()
                .stream()
                .filter(p -> StringUtils.equals(p.getName(), jpaSpecName))
                .findFirst()
                .orElseThrow();
    }

}
