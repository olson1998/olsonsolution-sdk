package com.olsonsolution.common.liquibase.domain.service;

import com.olsonsolution.common.liquibase.domain.model.exception.ConnectionFailedException;
import com.olsonsolution.common.liquibase.domain.model.exception.DataSourceConnectionException;
import com.olsonsolution.common.liquibase.domain.model.exception.LiquibaseCreationException;
import com.olsonsolution.common.liquibase.domain.model.exception.LiquibaseUpdateExecutionException;
import com.olsonsolution.common.migration.domain.model.DomainMigrationResult;
import com.olsonsolution.common.migration.domain.model.DomainMigrationResults;
import com.olsonsolution.common.migration.domain.port.repository.MigrationService;
import com.olsonsolution.common.migration.domain.port.stereotype.ChangeLog;
import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResult;
import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResults;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.ChangeLogMigrationException;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.ChangeLogSkippedException;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.MutableDateTime;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class LiquibaseMigrationService implements MigrationService {

    private static final int SKIPPED_RESULT_CODE = -1;

    private static final int FAILURE_RESULT_CODE = 0;

    private static final int SUCCESSFUL_RESULT_CODE = 1;

    private final Scheduler executorScheduler;

    private final ResourceAccessor resourceAccessor;

    private final ConcurrentMap<DataSource, CompletableFuture<Void>> ongoingDataSourceMigrations =
            new ConcurrentHashMap<>();

    private final ConcurrentMap<DataSource, ConcurrentLinkedQueue<Consumer<MigrationResults>>>
            dataSourceMigrationsResultsConsumers = new ConcurrentHashMap<>();

    @Override
    public void migrate(@NonNull DataSource dataSource, @NonNull Collection<? extends ChangeLog> changeLogs) {
        migrate(dataSource, changeLogs, null);
    }

    @Override
    @SneakyThrows
    public void migrate(@NonNull DataSource dataSource,
                        @NonNull Collection<? extends ChangeLog> changeLogs,
                        Consumer<MigrationResults> resultsConsumer) {
        migrateAsync(dataSource, changeLogs, resultsConsumer).get();
    }

    @Override
    public CompletableFuture<Void> migrateAsync(@NonNull DataSource dataSource,
                                                @NonNull Collection<? extends ChangeLog> changeLogs) {
        return migrateAsync(dataSource, changeLogs, null);
    }

    @Override
    public CompletableFuture<Void> migrateAsync(@NonNull DataSource dataSource,
                                                @NonNull Collection<? extends ChangeLog> changeLogs,
                                                Consumer<MigrationResults> resultsConsumer) {
        appendResultsConsumer(dataSource, resultsConsumer);
        return ongoingDataSourceMigrations.computeIfAbsent(
                dataSource,
                ds -> migrateChangeLogs(ds, changeLogs).toFuture()
        );
    }

    private Mono<Void> migrateChangeLogs(@NonNull DataSource dataSource,
                                         @NonNull Collection<? extends ChangeLog> changeLogs) {
        return createLiquibaseConnection(dataSource, changeLogs)
                .subscribeOn(executorScheduler)
                .flatMap(jdbcConnection -> migrateChangeLogs(jdbcConnection, changeLogs))
                .doOnNext(migrationResults -> publishResults(migrationResults, dataSource))
                .onErrorResume(ConnectionFailedException.class, this::supplyConnectionFailedResults)
                .flatMap(r -> {
                    ongoingDataSourceMigrations.remove(dataSource);
                    return Mono.empty();
                });
    }

    private Mono<MigrationResults> migrateChangeLogs(@NonNull JdbcConnection jdbcConnection,
                                                     @NonNull Collection<? extends ChangeLog> changeLogs) {
        return Flux.fromIterable(changeLogs)
                .flatMap(changeLog -> migrateChangeLog(jdbcConnection, changeLog))
                .collect(Collectors.collectingAndThen(Collectors.toList(), this::collectToResults))
                .flatMap(migrationResults -> closeJdbcConnectionAndReturn(
                        migrationResults,
                        jdbcConnection
                ));
    }

    private Mono<MigrationResult> migrateChangeLog(@NonNull JdbcConnection jdbcConnection,
                                                   @NonNull ChangeLog changeLog) {
        MutableDateTime startTimestamp = MutableDateTime.now();
        log.info("Liquibase migration changelog: '{}' started. Timestamp: '{}'", changeLog.getPath(), startTimestamp);
        return createLiquibase(changeLog, jdbcConnection)
                .flatMap(liquibase -> updateLiquibase(liquibase, changeLog, startTimestamp)
                        .flatMap(migrationResult -> closeLiquibaseAndReturn(migrationResult, liquibase)))
                .onErrorResume(
                        ChangeLogSkippedException.class,
                        e -> supplySkippedResult(e, startTimestamp)
                ).onErrorResume(
                        ChangeLogMigrationException.class,
                        e -> supplyFailedResult(e, startTimestamp)
                ).doOnNext(migrationResult -> logMigrationFinish(migrationResult, changeLog));
    }

    private Mono<MigrationResult> updateLiquibase(@NonNull Liquibase liquibase,
                                                  @NonNull ChangeLog changeLog,
                                                  @NonNull MutableDateTime startTimestamp) {
        return Mono.fromCallable(() -> executeUpdateLiquibase(liquibase, changeLog, startTimestamp))
                .onErrorMap(
                        LiquibaseException.class,
                        e -> new LiquibaseUpdateExecutionException(e, changeLog)
                );
    }

    private Mono<JdbcConnection> createLiquibaseConnection(@NonNull DataSource dataSource,
                                                           @NonNull Collection<? extends ChangeLog> changeLogs) {
        return createConnection(dataSource, changeLogs)
                .flatMap(connection -> Mono.fromCallable(() -> new JdbcConnection(connection)));
    }

    private Mono<Connection> createConnection(@NonNull DataSource dataSource,
                                              @NonNull Collection<? extends ChangeLog> changeLogs) {
        AtomicReference<MutableDateTime> attemptTimestamp = new AtomicReference<>(MutableDateTime.now());
        return Mono.fromCallable(() -> {
            attemptTimestamp.set(MutableDateTime.now());
            return dataSource.getConnection();
        }).onErrorMap(
                SQLException.class,
                e -> new ConnectionFailedException(e, attemptTimestamp.get(), changeLogs)
        );
    }

    private Mono<MigrationResult> closeLiquibaseAndReturn(MigrationResult migrationResult,
                                                          Liquibase liquibase) {
        return Mono.fromCallable(() -> closeLiquibase(liquibase, migrationResult))
                .onErrorResume(e -> Mono.fromSupplier(() -> logOnLiquibaseCloseError(e, migrationResult)));
    }

    private Mono<MigrationResults> closeJdbcConnectionAndReturn(MigrationResults migrationResults,
                                                                JdbcConnection jdbcConnection) {
        return Mono.fromCallable(() -> closeJdbcConnection(jdbcConnection, migrationResults))
                .onErrorResume(e -> Mono.fromSupplier(() -> logOnJdbcConnectionCloseError(
                        e,
                        migrationResults
                )));
    }

    private Mono<Liquibase> createLiquibase(@NonNull ChangeLog changeLog,
                                            @NonNull JdbcConnection jdbcConnection) {
        MutableDateTime startTimestamp = MutableDateTime.now();
        return Mono.fromCallable(() -> createLiquibase(jdbcConnection, changeLog))
                .onErrorMap(
                        LiquibaseException.class,
                        e -> new LiquibaseCreationException(e, changeLog, startTimestamp)
                );
    }

    private Mono<MigrationResult> supplySkippedResult(ChangeLogSkippedException e,
                                                      MutableDateTime startTimestamp) {
        return Mono.fromSupplier(() -> DomainMigrationResult.skippedResult()
                .skippingCause(e)
                .startTimestamp(startTimestamp)
                .finishTimestamp(MutableDateTime.now())
                .build());
    }

    private Mono<MigrationResult> supplyFailedResult(ChangeLogMigrationException e,
                                                     MutableDateTime startTimestamp) {
        return Mono.fromSupplier(() -> DomainMigrationResult.failedResult()
                .failureCause(e)
                .startTimestamp(startTimestamp)
                .finishTimestamp(MutableDateTime.now())
                .build());
    }

    private Mono<MigrationResults> supplyConnectionFailedResults(ConnectionFailedException e) {
        return Flux.fromIterable(e.getChangeLogs())
                .map(changeLog -> DomainMigrationResult.failedResult()
                        .createdSchema(false)
                        .failureCause(new DataSourceConnectionException(e, changeLog))
                        .startTimestamp(e.getConnectionAttemptTimestamp())
                        .finishTimestamp(e.getExceptionCreationTimestamp())
                        .build())
                .collect(Collectors.collectingAndThen(Collectors.toList(), this::collectToResults));
    }

    private Liquibase createLiquibase(@NonNull JdbcConnection jdbcConnection,
                                      @NonNull ChangeLog changeLog) throws LiquibaseException {
        String path = changeLog.getPath();
        Liquibase liquibase = new Liquibase(path, resourceAccessor, jdbcConnection);
        return liquibase;
    }

    private MigrationResult executeUpdateLiquibase(@NonNull Liquibase liquibase,
                                                   @NonNull ChangeLog changeLog,
                                                   @NonNull MutableDateTime startTimestamp) throws LiquibaseException {
        liquibase.update();
        return DomainMigrationResult.successfulResult()
                .startTimestamp(startTimestamp)
                .finishTimestamp(MutableDateTime.now())
                .createdSchema(changeLog.isCreateSchema())
                .build();
    }

    private MigrationResult closeLiquibase(Liquibase liquibase,
                                           MigrationResult migrationResult) throws LiquibaseException {
        liquibase.close();
        return migrationResult;
    }

    private MigrationResults closeJdbcConnection(JdbcConnection jdbcConnection,
                                                 MigrationResults migrationResults) throws LiquibaseException {
        jdbcConnection.close();
        return migrationResults;
    }

    private MigrationResults collectToResults(List<MigrationResult> migrationResults) {
        Collection<? extends MigrationResult> successfulResults =
                collectResults(migrationResults, SUCCESSFUL_RESULT_CODE);
        Collection<? extends MigrationResult> failureResults =
                collectResults(migrationResults, FAILURE_RESULT_CODE);
        Collection<? extends MigrationResult> skippedResults =
                collectResults(migrationResults, SKIPPED_RESULT_CODE);
        return new DomainMigrationResults(
                successfulResults,
                failureResults,
                skippedResults
        );
    }

    private Collection<? extends MigrationResult> collectResults(List<MigrationResult> migrationResults,
                                                                 int resultCode) {
        return migrationResults.stream()
                .filter(migrationResult -> isMatchingResultCode(migrationResult, resultCode))
                .toList();
    }

    private MigrationResult logOnLiquibaseCloseError(Throwable e,
                                                     MigrationResult migrationResult) {
        log.error("Failed to close liquibase, reason:", e);
        return migrationResult;
    }

    private MigrationResults logOnJdbcConnectionCloseError(Throwable e,
                                                           MigrationResults migrationResults) {
        log.error("Failed to close Jdbc connection, reason:", e);
        return migrationResults;
    }

    private void logMigrationFinish(MigrationResult migrationResult, ChangeLog changeLog) {
        log.info(
                "Liquibase migration changelog: '{}' finished. Timestamp: '{}'. Successful: '{}'",
                changeLog.getPath(),
                migrationResult.getFinishTimestamp(),
                migrationResult.isSuccessful()
        );
    }

    private void appendResultsConsumer(@NonNull DataSource dataSource,
                                       Consumer<MigrationResults> migrationResultsConsumer) {
        if (migrationResultsConsumer != null) {
            ConcurrentLinkedQueue<Consumer<MigrationResults>> consumersQueue =
                    dataSourceMigrationsResultsConsumers.computeIfAbsent(
                            dataSource, ds -> new ConcurrentLinkedQueue<>()
                    );
            boolean consumerAdded = consumersQueue.add(migrationResultsConsumer);
            if (!consumerAdded) {
                log.warn("Migration results consumer has not been added since results consumer is locked");
            }
        }
    }

    private void publishResults(MigrationResults migrationResults, DataSource dataSource) {
        if (dataSourceMigrationsResultsConsumers.containsKey(dataSource)) {
            ConcurrentLinkedQueue<Consumer<MigrationResults>> consumersQueue =
                    dataSourceMigrationsResultsConsumers.get(dataSource);
            for (Consumer<MigrationResults> consumer : consumersQueue) {
                publishResults(consumer, migrationResults);
            }
        }
    }

    private void publishResults(Consumer<MigrationResults> consumer, MigrationResults migrationResults) {
        try {
            consumer.accept(migrationResults);
        } catch (Exception e) {
            log.warn("Failed to publish migration results:", e);
        }
    }

    private boolean isMatchingResultCode(MigrationResult migrationResult, int resultCode) {
        if (resultCode == SKIPPED_RESULT_CODE) {
            return migrationResult.isSkipped();
        } else if (resultCode == FAILURE_RESULT_CODE) {
            return migrationResult.isFailed();
        } else if (resultCode == SUCCESSFUL_RESULT_CODE) {
            return migrationResult.isSuccessful();
        }
        return false;
    }

}
