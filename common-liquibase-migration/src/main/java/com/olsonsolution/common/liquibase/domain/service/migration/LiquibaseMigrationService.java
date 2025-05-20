package com.olsonsolution.common.liquibase.domain.service.migration;

import com.olsonsolution.common.data.domain.port.datasource.SqlVendorAwareDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.liquibase.domain.model.exception.LiquibaseExecutionException;
import com.olsonsolution.common.liquibase.domain.model.exception.SqlVendorNotSupportedException;
import com.olsonsolution.common.migration.domain.model.DomainMigrationResult;
import com.olsonsolution.common.migration.domain.model.DomainMigrationResults;
import com.olsonsolution.common.migration.domain.model.DomainSchemaParseResult;
import com.olsonsolution.common.migration.domain.port.repository.MigrationService;
import com.olsonsolution.common.migration.domain.port.repository.SqlVendorSupporter;
import com.olsonsolution.common.migration.domain.port.repository.VariablesProvider;
import com.olsonsolution.common.migration.domain.port.stereotype.*;
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
import java.util.*;
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

    private final List<SqlVendorSupporter> sqlVendorSupporters;

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
        SqlVendorSupporter sqlVendorSupporter;
        try {
            sqlVendorSupporter = findSqlVendorSupporter(dataSource);
        } catch (SqlVendorNotSupportedException e) {
            return ofSqlVendorNotSupported(e);
        }
        return changeLogs.stream()
                .map(changeLog -> migrate(changeLog, dataSource, sqlVendorSupporter))
                .collect(Collectors.collectingAndThen(Collectors.toList(), DomainMigrationResults::fromResults));
    }

    private MigrationResult migrate(ChangeLog changeLog, DataSource dataSource, SqlVendorSupporter sqlVendorSupporter) {
        MutableDateTime startTimestamp = timeUtils.getTimestamp();
        Map<String, SchemaParseResult> createdSchemas = new HashMap<>();
        try (Connection connection = dataSource.getConnection();
             DatabaseConnection liquibaseConnection = new JdbcConnection(connection);
             Liquibase liquibase = new Liquibase(changeLog.getPath(), resourceAccessor, liquibaseConnection)) {
            variablesProviders.forEach(variablesProvider -> variablesProvider.getVariables()
                    .forEach(liquibase::setChangeLogParameter));
            sqlVendorSupporter.getTypeVariables().forEach(liquibase::setChangeLogParameter);
            createSchemasIfEnabled(changeLog, dataSource, sqlVendorSupporter, createdSchemas);
            liquibase.update();
            return DomainMigrationResult.successfulResult()
                    .startTimestamp(startTimestamp)
                    .finishTimestamp(timeUtils.getTimestamp())
                    .createdSchemas(createdSchemas)
                    .build();
        } catch (LiquibaseException | SQLException e) {
            log.error(
                    "Failed to migrate change log: {}, data source={}",
                    changeLog.getPath(), dataSource, e
            );
            return DomainMigrationResult.failedResult()
                    .createdSchemas(createdSchemas)
                    .startTimestamp(startTimestamp)
                    .finishTimestamp(timeUtils.getTimestamp())
                    .failureCause(new LiquibaseExecutionException(e, changeLog))
                    .build();
        }
    }

    private void createSchemasIfEnabled(ChangeLog changeLog,
                                        DataSource dataSource,
                                        SqlVendorSupporter sqlVendorSupporter,
                                        Map<String, SchemaParseResult> createdSchemas) throws SQLException {
        for (Map.Entry<String, SchemaConfig> schemaConfig : changeLog.getSchemas().entrySet()) {
            String schema = schemaConfig.getKey();
            SchemaConfig config = schemaConfig.getValue();
            SchemaParseResult result;
            if (config.isCreateSchema()) {
                result = createSchema(schema, dataSource, sqlVendorSupporter);
            } else {
                result = verifySchema(schema, dataSource, sqlVendorSupporter);
            }
            createdSchemas.put(schema, result);
        }
    }

    private SchemaParseResult createSchema(String schema,
                                           DataSource dataSource,
                                           SqlVendorSupporter sqlVendorSupporter) throws SQLException {
        boolean schemaCreated = false;
        DomainSchemaParseResult.DomainSchemaParseResultBuilder result = DomainSchemaParseResult.builder()
                .createSchemaEnabled(true);
        boolean schemaExists = sqlVendorSupporter.existsSchema(dataSource, schema);
        result.schemaExists(schemaExists);
        if (schemaExists) {
            log.info("Schema '{}' already exists", schema);
        } else {
            log.info("Schema '{}' does not exist, creating...", schema);
            sqlVendorSupporter.createSchema(dataSource, schema);
            schemaCreated = true;
        }
        result.schemaCreated(schemaCreated);
        return result.build();
    }

    private SchemaParseResult verifySchema(String schema,
                                           DataSource dataSource,
                                           SqlVendorSupporter sqlVendorSupporter) throws SQLException {
        boolean schemaExists = sqlVendorSupporter.existsSchema(dataSource, schema);
        log.info("Schema '{}' exists?: {}", schema, schemaExists);
        return DomainSchemaParseResult.disabled(schemaExists);
    }

    private SqlVendorSupporter findSqlVendorSupporter(DataSource dataSource) throws SqlVendorNotSupportedException {
        Optional<SqlVendorSupporter> sqlVendorSupporter;
        MutableDateTime startTimestamp = timeUtils.getTimestamp();
        if (dataSource instanceof SqlVendorAwareDataSource sqlVendorAwareDataSource) {
            sqlVendorSupporter = findSqlVendorSupporter(sqlVendorAwareDataSource.getVendor());
        } else {
            sqlVendorSupporter = assumeSqlVendorSupporter(dataSource);
        }
        return sqlVendorSupporter.orElseThrow(() -> new SqlVendorNotSupportedException(dataSource, startTimestamp));
    }

    private Optional<SqlVendorSupporter> findSqlVendorSupporter(SqlVendor sqlVendor) {
        return sqlVendorSupporters.stream()
                .filter(sqlVendorSupporter -> sqlVendorSupporter.getVendor()
                        .isSameAs(sqlVendor))
                .findFirst();
    }

    private Optional<SqlVendorSupporter> assumeSqlVendorSupporter(DataSource dataSource) {
        return sqlVendorSupporters.stream()
                .filter(sqlVendorSupporter -> sqlVendorSupporter.getSupportedDataSourceClasses()
                        .stream()
                        .anyMatch(vendor -> vendor.isInstance(dataSource)))
                .findFirst();
    }

    private MigrationResults ofSqlVendorNotSupported(SqlVendorNotSupportedException e) {
        return changeLogs.stream()
                .map(changeLog -> DomainMigrationResult.failedResult()
                        .createdSchemas(Collections.emptyMap())
                        .startTimestamp(e.getStartTimestamp())
                        .finishTimestamp(timeUtils.getTimestamp())
                        .failureCause(new LiquibaseExecutionException(e, changeLog))
                        .build())
                .collect(Collectors.collectingAndThen(
                        Collectors.toUnmodifiableList(),
                        DomainMigrationResults::fromResults
                ));
    }

}
