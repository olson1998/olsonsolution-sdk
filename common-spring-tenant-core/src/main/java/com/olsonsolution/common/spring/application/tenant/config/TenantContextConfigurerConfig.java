package com.olsonsolution.common.spring.application.tenant.config;

import com.olsonsolution.common.spring.domain.port.repository.context.LocalContextManager;
import com.olsonsolution.common.spring.domain.port.repository.tenant.TenantContextAware;
import com.olsonsolution.common.spring.domain.port.repository.tenant.TenantContextAwaresConfigurer;
import com.olsonsolution.common.spring.domain.service.tenant.TenantContextAwareConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class TenantContextConfigurerConfig {

    @Bean
    public TenantContextAwaresConfigurer tenantContextAwaresConfigurer(LocalContextManager localContextManager,
                                                                       List<TenantContextAware> awares) {
        return new TenantContextAwareConfigurationService(localContextManager, awares);
    }

}
