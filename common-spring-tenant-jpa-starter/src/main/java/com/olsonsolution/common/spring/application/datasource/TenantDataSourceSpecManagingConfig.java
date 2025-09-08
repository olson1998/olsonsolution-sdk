package com.olsonsolution.common.spring.application.datasource;

import com.olsonsolution.common.spring.domain.port.repository.context.LocalContextManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.TenantDataSourceSpecManager;
import com.olsonsolution.common.spring.domain.service.datasource.TenantDataSourceSpecManagingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import static com.olsonsolution.common.spring.application.jpa.config.DataSourceSpecConfig.DATA_SOURCE_SPEC_MANAGER_TOGGLE_CONFIG;

@Configuration
@PropertySource("classpath:./application-spring-tenant-jpa.properties")
public class TenantDataSourceSpecManagingConfig {

    public static final String TENANT_DATA_SOURCE_SPEC_MANAGER = "tenant";

    @Bean
    @ConditionalOnProperty(
            value = DATA_SOURCE_SPEC_MANAGER_TOGGLE_CONFIG,
            havingValue = TENANT_DATA_SOURCE_SPEC_MANAGER
    )
    public TenantDataSourceSpecManager tenantDataSourceSpecManager(LocalContextManager localContextManager) {
        return new TenantDataSourceSpecManagingService(localContextManager);
    }

}
