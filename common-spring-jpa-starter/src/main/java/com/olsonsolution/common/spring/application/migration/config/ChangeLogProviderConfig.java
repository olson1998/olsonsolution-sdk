package com.olsonsolution.common.spring.application.migration.config;

import com.olsonsolution.common.migration.domain.port.repository.ChangelogProvider;
import com.olsonsolution.common.spring.domain.port.config.jpa.JpaSpecConfig;
import com.olsonsolution.common.spring.domain.service.migration.GeneratedChangeLogProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChangeLogProviderConfig {

    @Bean
    public ChangelogProvider generatedChangeLogProvider(List<JpaSpecConfig> jpaSpecConfigs) {
        return new GeneratedChangeLogProvider(jpaSpecConfigs);
    }

}
