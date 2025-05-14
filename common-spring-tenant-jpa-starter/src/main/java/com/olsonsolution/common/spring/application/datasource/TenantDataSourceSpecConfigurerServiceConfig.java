package com.olsonsolution.common.spring.application.datasource;

import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.tenant.TenantContextAware;
import com.olsonsolution.common.spring.domain.service.datasource.TenantDataSourceSpecConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TenantDataSourceSpecConfigurerServiceConfig {

    @Bean
    public TenantContextAware tenantDataSourceSpecConfigurerService(DataSourceSpecManager dataSourceSpecManager) {
        return new TenantDataSourceSpecConfigurationService(dataSourceSpecManager);
    }

}
