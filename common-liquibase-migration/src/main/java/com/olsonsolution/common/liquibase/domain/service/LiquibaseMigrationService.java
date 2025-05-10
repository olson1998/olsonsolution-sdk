package com.olsonsolution.common.liquibase.domain.service;

import com.olsonsolution.common.liquibase.domain.model.exception.LiquibaseExecutionException;
import com.olsonsolution.common.migration.domain.model.DomainMigrationResult;
import com.olsonsolution.common.migration.domain.model.DomainMigrationResults;
import com.olsonsolution.common.migration.domain.port.repository.MigrationService;
import com.olsonsolution.common.migration.domain.port.repository.VariablesProvider;
import com.olsonsolution.common.migration.domain.port.stereotype.ChangeLog;
import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResult;
import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResults;
import com.olsonsolution.common.time.domain.port.TimeUtils;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.MutableDateTime;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class LiquibaseMigrationService implements MigrationService {

    private final Executor executor;

    private final TimeUtils timeUtils;

    private final ResourceAccessor resourceAccessor;

    private final List<? extends ChangeLog> changeLogs;

    private final List<VariablesProvider> variablesProviders;

    private final ConcurrentMap<DataSource, CompletableFuture<MigrationResults>> ongoingAsyncMigrations =
            new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<MigrationResults> migrateAsync(DataSource dataSource) {
        return ongoingAsyncMigrations.computeIfAbsent(dataSource, this::initAsyncMigration);
    }

    private CompletableFuture<MigrationResults> initAsyncMigration(DataSource dataSource) {
        return CompletableFuture.supplyAsync(() -> migrate(dataSource), executor);
    }

    private MigrationResults migrate(DataSource dataSource) {
        return changeLogs.stream()
                .map(changeLog -> migrate(changeLog, dataSource))
                .collect(Collectors.collectingAndThen(Collectors.toList(), DomainMigrationResults::fromResults));
    }

    private MigrationResult migrate(ChangeLog changeLog, DataSource dataSource) {
        MutableDateTime startTimestamp = timeUtils.getTimestamp();
        try (Connection connection = dataSource.getConnection();
             DatabaseConnection liquibaseConnection = new JdbcConnection(connection);
             Liquibase liquibase = new Liquibase(changeLog.getPath(), resourceAccessor, liquibaseConnection)) {
            variablesProviders.forEach(variablesProvider -> variablesProvider.getVariables()
                    .forEach(liquibase::setChangeLogParameter));
            liquibase.update();
            return DomainMigrationResult.successfulResult()
                    .startTimestamp(startTimestamp)
                    .finishTimestamp(timeUtils.getTimestamp())
                    .createdSchema(changeLog.isCreateSchema())
                    .build();
        } catch (LiquibaseException | SQLException e) {
            log.error(
                    "Failed to migrate change log: {} schema: '{}', data source={}",
                    changeLog.getPath(), changeLog.getSchema(), dataSource, e
            );
            return DomainMigrationResult.failedResult()
                    .createdSchema(changeLog.isCreateSchema())
                    .startTimestamp(startTimestamp)
                    .finishTimestamp(timeUtils.getTimestamp())
                    .failureCause(new LiquibaseExecutionException(e, changeLog))
                    .build();
        }
    }

}
