package com.olsonsolution.common.spring.application.migration.config;

import com.olsonsolution.common.liquibase.domain.service.migration.LiquibaseMigrationService;
import com.olsonsolution.common.migration.domain.port.repository.ChangelogProvider;
import com.olsonsolution.common.migration.domain.port.repository.MigrationService;
import com.olsonsolution.common.migration.domain.port.repository.SqlVendorSupporter;
import com.olsonsolution.common.migration.domain.port.repository.VariablesProvider;
import com.olsonsolution.common.migration.domain.port.stereotype.ChangeLog;
import com.olsonsolution.common.spring.application.migration.props.LiquibaseProperties;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.service.migration.LiquibaseMigrationTaskDecorator;
import com.olsonsolution.common.time.domain.port.TimeUtils;
import liquibase.integration.spring.SpringResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.List;
import java.util.concurrent.Executor;

@Configuration
public class LiquibaseConfig {

    public static final String LIQUIBASE_EXECUTOR_BEAN = "liquibaseExecutor";

    @Bean(LIQUIBASE_EXECUTOR_BEAN)
    public Executor liquibaseExecutor(LiquibaseProperties liquibaseProperties,
                                      DataSourceSpecManager dataSourceSpecManager) {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setTaskDecorator(new LiquibaseMigrationTaskDecorator(dataSourceSpecManager));
        executor.setConcurrencyLimit(liquibaseProperties.getExecutor().getPoolSize());
        executor.setTaskTerminationTimeout(liquibaseProperties.getExecutor().getTerminationTimeout().toMillis());
        return executor;
    }

    @Bean
    public MigrationService migrationService(@Qualifier(LIQUIBASE_EXECUTOR_BEAN) Executor executor,
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
