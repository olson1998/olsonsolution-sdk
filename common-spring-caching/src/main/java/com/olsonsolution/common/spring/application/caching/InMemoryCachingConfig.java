package com.olsonsolution.common.spring.application.caching;

import com.olsonsolution.common.caching.domain.port.repository.InMemoryCacheFactory;
import com.olsonsolution.common.caching.domain.service.InMemoryFabricatingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InMemoryCachingConfig {

    @Bean
    public InMemoryCacheFactory inMemoryCacheFactory() {
        return new InMemoryFabricatingService();
    }

}
