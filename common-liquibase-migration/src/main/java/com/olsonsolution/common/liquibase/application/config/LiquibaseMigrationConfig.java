package com.olsonsolution.common.liquibase.application.config;

import com.olsonsolution.common.liquibase.application.props.LiquibaseMigrationProperties;
import com.olsonsolution.common.liquibase.domain.service.LiquibaseMigrationService;
import com.olsonsolution.common.migration.domain.port.repository.MigrationService;
import com.olsonsolution.common.time.domain.port.TimeUtils;
import liquibase.integration.spring.SpringResourceAccessor;
import liquibase.resource.ResourceAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration
@RequiredArgsConstructor
public class LiquibaseMigrationConfig {

    private final LiquibaseMigrationProperties liquibaseMigrationProperties;

    @Bean
    public MigrationService liquibaseMigrationService(ResourceLoader resourceLoader,
                                                      TimeUtils timeUtils) {
        ResourceAccessor resourceAccessor = new SpringResourceAccessor(resourceLoader);
        return new LiquibaseMigrationService(
                null,
                resourceAccessor,
                timeUtils
        );
    }

}
