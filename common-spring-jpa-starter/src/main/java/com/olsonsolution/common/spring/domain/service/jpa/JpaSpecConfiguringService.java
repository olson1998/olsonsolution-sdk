package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.spring.domain.model.exception.jpa.JpaSpecNotRegisteredException;
import com.olsonsolution.common.spring.domain.port.props.jpa.JpaProperties;
import com.olsonsolution.common.spring.domain.port.props.jpa.JpaSpecProperties;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaSpecDataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.EntityManagerFactoryDelegate;
import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaSpecConfigurer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
public class JpaSpecConfiguringService implements JpaSpecConfigurer {

    private final JpaProperties jpaProperties;

    @Override
    public boolean resolveCreateSchema(@NonNull String jpaSpec) {
        JpaSpecProperties properties = getJpaSpecProperties(jpaSpec);
        return properties.isCreateSchema();
    }

    @Override
    public String resolveSchema(@NonNull String jpaSpecName) {
        JpaSpecProperties properties = getJpaSpecProperties(jpaSpecName);
        return properties.getSchema();
    }

    @Override
    public EntityManagerFactoryDelegate createEntityManagerFactoryDelegate(
            @NonNull String jpaSpecName,
            @NonNull String[] entityBasePackages,
            DataSourceSpecManager dataSourceSpecManager,
            JpaSpecDataSourceSpecManager jpaSpecDataSourceSpecManager,
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
                jpaSpecDataSourceSpecManager,
                sqlDataSourceProvider,
                destinationDataSourceManager
        );
    }

    @Override
    public PlatformTransactionManager createPlatformTransactionManager(EntityManagerFactoryDelegate em) {
        return new JpaTransactionManager(em);
    }

    private JpaSpecProperties getJpaSpecProperties(String jpaSpecName) {
        return jpaProperties.getJpaSpecificationsProperties()
                .stream()
                .filter(p -> StringUtils.equals(p.getName(), jpaSpecName))
                .findFirst()
                .orElseThrow(() -> JpaSpecNotRegisteredException.forName(jpaSpecName));
    }


}
