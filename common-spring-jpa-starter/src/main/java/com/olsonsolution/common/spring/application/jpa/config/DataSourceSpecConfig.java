package com.olsonsolution.common.spring.application.jpa.config;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.spring.application.jpa.props.ApplicationSqlDataSourceProperties;
import com.olsonsolution.common.spring.application.jpa.props.ApplicationSqlUserProperties;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DataSourceEvictor;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaSpecDataSourceSpecManager;
import com.olsonsolution.common.spring.domain.service.datasource.DataSourceEvictionService;
import com.olsonsolution.common.spring.domain.service.datasource.DataSourceSpecManagingService;
import com.olsonsolution.common.spring.domain.service.datasource.DataSourceSpecificationManagingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.H2;
import static com.olsonsolution.common.spring.application.jpa.props.SpringApplicationJpaProperties.SPRING_APPLICATION_PROPERTIES_PREFIX;

@Configuration
public class DataSourceSpecConfig {

    public static final String ROUTING_DATA_SOURCE_EVICTOR_BEAN = "routingDataSourceEvictor";

    public static final String H2_INITIAL_DATA_SOURCE_NAME = "H2_INITIAL";

    public static final SqlDataSource H2_INITIAL_DATA_SOURCE = initialDataSource();

    public static final String DATA_SOURCE_SPEC_MANAGER_TOGGLE_CONFIG =
            SPRING_APPLICATION_PROPERTIES_PREFIX + ".data-source.spec-manager.toggle";

    public static final String DEFAULT_DATA_SOURCE_SPEC_MANAGER = "default";

    @Bean(ROUTING_DATA_SOURCE_EVICTOR_BEAN)
    public DataSourceEvictor routingDataSourceEvictor() {
        return new DataSourceEvictionService();
    }

    @Bean
    public DataSourceSpecManager dataSourceSpecManager() {
        return new DataSourceSpecManagingService();
    }

    @Bean
    @ConditionalOnProperty(
            value = DATA_SOURCE_SPEC_MANAGER_TOGGLE_CONFIG,
            havingValue = DEFAULT_DATA_SOURCE_SPEC_MANAGER,
            matchIfMissing = true
    )
    public JpaSpecDataSourceSpecManager jpaSpecDataSourceSpecManager() {
        JpaSpecDataSourceSpecManager manager = new DataSourceSpecificationManagingService();
        return manager;
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
                "pass",
                ""
        );
        dataSource.getUser().getRwx().add(user);
        return dataSource;
    }

}
