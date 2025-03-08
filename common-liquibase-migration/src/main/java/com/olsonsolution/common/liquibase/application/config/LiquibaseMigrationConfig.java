package com.olsonsolution.common.liquibase.application.config;

import com.olsonsolution.common.liquibase.application.props.ApplicationLiquibaseManagerProperties;
import com.olsonsolution.common.liquibase.domain.port.repository.LiquibaseContextProvider;
import com.olsonsolution.common.liquibase.domain.service.LiquibaseMigrationService;
import com.olsonsolution.common.migration.domain.port.repository.ChangelogProvider;
import com.olsonsolution.common.migration.domain.port.repository.MigrationService;
import com.olsonsolution.common.migration.domain.port.repository.SqlVendorVariablesProvider;
import com.olsonsolution.common.migration.domain.port.repository.VariablesProvider;
import com.olsonsolution.common.time.domain.port.TimeUtils;
import liquibase.integration.spring.SpringResourceAccessor;
import liquibase.resource.ResourceAccessor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
@RequiredArgsConstructor
public class LiquibaseMigrationConfig {

    @Bean
    public MigrationService liquibaseMigrationService(ApplicationLiquibaseManagerProperties liquibaseManagerProperties,
                                                      TimeUtils timeUtils,
                                                      ResourceLoader resourceLoader,
                                                      @Nullable LiquibaseContextProvider liquibaseContextProvider,
                                                      List<? extends ChangelogProvider> changelogProviders,
                                                      List<? extends VariablesProvider> variablesProviders,
                                                      List<? extends SqlVendorVariablesProvider> sqlVendorVarsProv) {
        ApplicationLiquibaseManagerProperties.ExecutorProperties properties = liquibaseManagerProperties.getExecutor();
        ThreadFactory threadFactory = new BasicThreadFactory.Builder()
                .namingPattern("liquibase-%s")
                .build();
        Executor executor = Executors.newFixedThreadPool(
                properties.getThreads(),
                threadFactory
        );
        Scheduler scheduler = Schedulers.fromExecutor(executor);
        ResourceAccessor resourceAccessor = new SpringResourceAccessor(resourceLoader);
        return new LiquibaseMigrationService(
                liquibaseManagerProperties,
                scheduler,
                timeUtils,
                resourceAccessor,
                liquibaseContextProvider,
                changelogProviders,
                variablesProviders,
                sqlVendorVarsProv
        );
    }

}
