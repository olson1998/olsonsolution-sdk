package com.olsonsolution.common.spring.application.config.context;

import com.olsonsolution.common.spring.domain.port.repository.context.LocalContextManager;
import com.olsonsolution.common.spring.domain.service.context.LocalContextManagingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalContextManagerConfig {

    @Bean
    public LocalContextManager localContextManager() {
        return new LocalContextManagingService();
    }

}
