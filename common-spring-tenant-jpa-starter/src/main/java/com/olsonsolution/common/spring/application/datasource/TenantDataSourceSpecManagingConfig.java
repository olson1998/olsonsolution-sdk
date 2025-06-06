package com.olsonsolution.common.spring.application.datasource;

import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaSpecDataSourceSpecManager;
import com.olsonsolution.common.spring.domain.service.datasource.TenantDataSourceSpecManagingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TenantDataSourceSpecManagingConfig {

    @Bean
    public JpaSpecDataSourceSpecManager tenantDataSourceSpecManager() {
        return new TenantDataSourceSpecManagingService();
    }

}
