package com.olsonsolution.common.spring.application.migration.config;

import com.olsonsolution.common.liquibase.domain.service.migration.LiquibaseMigrationService;
import com.olsonsolution.common.migration.domain.port.repository.ChangelogProvider;
import com.olsonsolution.common.migration.domain.port.repository.MigrationService;
import com.olsonsolution.common.migration.domain.port.repository.SqlVendorSupporter;
import com.olsonsolution.common.migration.domain.port.repository.VariablesProvider;
import com.olsonsolution.common.migration.domain.port.stereotype.ChangeLog;
import com.olsonsolution.common.time.domain.port.TimeUtils;
import liquibase.integration.spring.SpringResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import java.util.List;
import java.util.concurrent.Executor;

import static com.olsonsolution.common.spring.application.async.config.AsyncConfig.SYSTEM_EXECUTOR_BEAN;

@Configuration
public class LiquibaseConfig {

    @Bean
    public MigrationService migrationService(@Qualifier(SYSTEM_EXECUTOR_BEAN) Executor executor,
                                             TimeUtils timeUtils,
                                             ResourceLoader resourceLoader,
                                             List<ChangelogProvider> changelogProviders,
                                             List<VariablesProvider> variablesProviders,
                                             List<SqlVendorSupporter> sqlVendorSupporters) {
        ResourceAccessor resourceAccessor = new SpringResourceAccessor(resourceLoader);
        List<? extends ChangeLog> changeLogs = changelogProviders.stream()
                .flatMap(changelogProvider -> changelogProvider.getChangelogs().stream())
                .toList();
        return new LiquibaseMigrationService(
                executor,
                timeUtils,
                resourceAccessor,
                changeLogs,
                variablesProviders,
                sqlVendorSupporters
        );
    }

}
