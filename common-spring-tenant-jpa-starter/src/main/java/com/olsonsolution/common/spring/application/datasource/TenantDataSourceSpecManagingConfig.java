package com.olsonsolution.common.spring.application.datasource;

import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.service.datasource.TenantDataSourceSpecManagingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TenantDataSourceSpecManagingConfig {

    @Bean
    public DataSourceSpecManager tenantDataSourceSpecManager() {
        return new TenantDataSourceSpecManagingService();
    }

}
