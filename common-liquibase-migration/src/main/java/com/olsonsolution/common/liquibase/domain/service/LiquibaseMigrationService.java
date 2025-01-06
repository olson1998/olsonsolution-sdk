package com.olsonsolution.common.liquibase.domain.service;

import com.olsonsolution.common.liquibase.domain.model.exception.ConnectionFailedException;
import com.olsonsolution.common.liquibase.domain.model.exception.DataSourceConnectionException;
import com.olsonsolution.common.liquibase.domain.model.exception.LiquibaseCreationException;
import com.olsonsolution.common.liquibase.domain.model.exception.LiquibaseUpdateExecutionException;
import com.olsonsolution.common.migration.domain.model.DomainMigrationResult;
import com.olsonsolution.common.migration.domain.model.DomainMigrationResults;
import com.olsonsolution.common.migration.domain.port.repository.ChangelogProvider;
import com.olsonsolution.common.migration.domain.port.repository.MigrationResultsPublisher;
import com.olsonsolution.common.migration.domain.port.repository.MigrationService;
import com.olsonsolution.common.migration.domain.port.stereotype.ChangeLog;
import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResult;
import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResults;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.ChangeLogMigrationException;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.ChangeLogSkippedException;
import com.olsonsolution.common.time.domain.port.TimeUtils;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.joda.time.MutableDateTime;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;

@Slf4j
public class LiquibaseMigrationService implements MigrationService {

    private static final int SKIPPED_RESULT_CODE = -1;

    private static final int FAILURE_RESULT_CODE = 0;

    private static final int SUCCESSFUL_RESULT_CODE = 1;

    private static final Comparator<KeyValue<MigrationResultsPublisher, ?>> PUBLISHER_PRIORITY = Comparator
            .comparing(LiquibaseMigrationService::resolvePriority)
            .thenComparing(LiquibaseMigrationService::resolveSubTimestamp);

    private final Collection<? extends ChangeLog> changelogs;

    private final Scheduler executorScheduler;

    private final TimeUtils timeUtils;

    private final ResourceAccessor resourceAccessor;

    private final ConcurrentMap<DataSource, LiquibaseAsyncMigration> ongoingMigrations;

    public LiquibaseMigrationService(Scheduler executorScheduler,
                                     TimeUtils timeUtils,
                                     ResourceAccessor resourceAccessor,
                                     Collection<ChangelogProvider> changelogProviders) {
        this.changelogs = changelogProviders.stream()
                .flatMap(changelogProvider -> changelogProvider.getChangelogs().stream())
                .toList();
        this.executorScheduler = executorScheduler;
        this.timeUtils = timeUtils;
        this.resourceAccessor = resourceAccessor;
        this.ongoingMigrations = new ConcurrentHashMap<>();
    }

    @Override
    public Boolean migrate(DataSource dataSource, MigrationResultsPublisher publisher) {
        ongoingMigrations.computeIfAbsent(dataSource, this::innitAsyncMigration);
        appendResultsPublisher(dataSource, publisher);
        return publisher.getPublishedObservable()
                .asMono()
                .block();
    }

    @Override
    public CompletableFuture<Boolean> migrateAsync(DataSource dataSource, MigrationResultsPublisher publisher) {
        ongoingMigrations.computeIfAbsent(dataSource, this::innitAsyncMigration);
        appendResultsPublisher(dataSource, publisher);
        return publisher.getPublishedObservable()
                .asMono()
                .toFuture();
    }

    private static int resolvePriority(KeyValue<MigrationResultsPublisher, ?> registeredPublisher) {
        return registeredPublisher.getKey().getPriority();
    }

    private static MutableDateTime resolveSubTimestamp(KeyValue<MigrationResultsPublisher, ?> registeredPublisher) {
        return registeredPublisher.getKey().getSubscriptionTimestamp();
    }

    @NonNull
    private LiquibaseAsyncMigration innitAsyncMigration(@NonNull DataSource dataSource) {
        CompletableFuture<Void> migration = createLiquibaseConnection(dataSource, changelogs)
                .subscribeOn(executorScheduler)
                .flatMap(this::migrateChangeLogs)
                .onErrorResume(ConnectionFailedException.class, this::supplyConnectionFailedResults)
                .flatMap(migrationResults -> publishResults(migrationResults, dataSource))
                .toFuture();
        return new LiquibaseAsyncMigration(migration, new ConcurrentLinkedQueue<>());
    }

    private Mono<MigrationResults> migrateChangeLogs(@NonNull JdbcConnection jdbcConnection) {
        return Flux.fromIterable(changelogs)
                .flatMap(changeLog -> migrateChangeLog(jdbcConnection, changeLog))
                .collect(Collectors.collectingAndThen(Collectors.toList(), this::collectToResults))
                .flatMap(migrationResults -> closeJdbcConnectionAndReturn(
                        migrationResults,
                        jdbcConnection
                ));
    }

    private Mono<MigrationResult> migrateChangeLog(@NonNull JdbcConnection jdbcConnection,
                                                   @NonNull ChangeLog changeLog) {
        MutableDateTime startTimestamp = timeUtils.getTimestamp();
        log.info(
                "Liquibase migration changelog: '{}' started. Timestamp: '{}'",
                changeLog.getPath(),
                timeUtils.writeTimestamp(startTimestamp)
        );
        return createLiquibase(changeLog, jdbcConnection)
                .flatMap(liquibase -> updateLiquibase(liquibase, changeLog, startTimestamp)
                        .flatMap(migrationResult -> closeLiquibaseAndReturn(migrationResult, liquibase)))
                .onErrorResume(
                        ChangeLogSkippedException.class,
                        e -> supplySkippedResult(e, startTimestamp)
                ).onErrorResume(
                        ChangeLogMigrationException.class,
                        e -> supplyFailedResult(e, startTimestamp)
                ).doOnNext(migrationResult -> onMigrationFinished(migrationResult, changeLog));
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
        AtomicReference<MutableDateTime> attemptTimestamp = new AtomicReference<>(timeUtils.getTimestamp());
        return Mono.fromCallable(() -> {
            attemptTimestamp.set(timeUtils.getTimestamp());
            return dataSource.getConnection();
        }).onErrorMap(
                SQLException.class,
                e -> new ConnectionFailedException(e, attemptTimestamp.get(), changeLogs)
        );
    }

    private Mono<MigrationResult> closeLiquibaseAndReturn(MigrationResult migrationResult,
                                                          Liquibase liquibase) {
        return Mono.fromCallable(() -> closeLiquibase(liquibase, migrationResult))
                .onErrorResume(e -> Mono.fromSupplier(() -> onLiquibaseCloseException(e, migrationResult)));
    }

    private Mono<MigrationResults> closeJdbcConnectionAndReturn(MigrationResults migrationResults,
                                                                JdbcConnection jdbcConnection) {
        return Mono.fromCallable(() -> closeJdbcConnection(jdbcConnection, migrationResults))
                .onErrorResume(e -> Mono.fromSupplier(() -> onConnectionException(
                        e,
                        migrationResults
                )));
    }

    private Mono<Liquibase> createLiquibase(@NonNull ChangeLog changeLog,
                                            @NonNull JdbcConnection jdbcConnection) {
        MutableDateTime startTimestamp = timeUtils.getTimestamp();
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
                .finishTimestamp(timeUtils.getTimestamp())
                .build());
    }

    private Mono<MigrationResult> supplyFailedResult(ChangeLogMigrationException e,
                                                     MutableDateTime startTimestamp) {
        return Mono.fromSupplier(() -> DomainMigrationResult.failedResult()
                .failureCause(e)
                .startTimestamp(startTimestamp)
                .finishTimestamp(timeUtils.getTimestamp())
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

    private void appendResultsPublisher(@NonNull DataSource dataSource, MigrationResultsPublisher publisher) {
        if (publisher != null && ongoingMigrations.containsKey(dataSource)) {
            LiquibaseAsyncMigration asyncMigration = ongoingMigrations.get(dataSource);
            ConcurrentLinkedQueue<MigrationResultsPublisher> publishersQueue = asyncMigration.subscribingPublishers();
            boolean consumerAdded = publishersQueue.add(publisher);
            if (!consumerAdded) {
                log.warn(
                        "Migration results publisher: '{}' has not been added since queue is locked",
                        publisher.getOperationName()
                );
            } else {
                publisher.setSubscriptionTimestamp(timeUtils.getTimestamp());
            }
        } else if (publisher != null && !ongoingMigrations.containsKey(dataSource)) {
            log.warn("Migration results publisher: '{}' has not been added since" +
                    " there is no ongoing migration for data source", publisher.getOperationName());
        }
    }

    private Mono<Void> publishResults(MigrationResults migrationResults, DataSource dataSource) {
        return Optional.ofNullable(ongoingMigrations.get(dataSource))
                .map(LiquibaseAsyncMigration::subscribingPublishers)
                .map(queue -> publishResults(queue, migrationResults))
                .orElseGet(Flux::empty)
                .collectList()
                .flatMap(consumptionResults -> onPublishedResults(
                        consumptionResults,
                        migrationResults,
                        dataSource
                ));
    }

    private Flux<Boolean> publishResults(ConcurrentLinkedQueue<MigrationResultsPublisher> publishersQueue,
                                         MigrationResults migrationResults) {
        Stream.Builder<KeyValue<MigrationResultsPublisher, Mono<Void>>> registeredPublishers = Stream.builder();
        Stream.Builder<KeyValue<MigrationResultsPublisher, Mono<Void>>> asyncRegisteredPublishers = Stream.builder();
        for (MigrationResultsPublisher publisher : publishersQueue) {
            onRegisterPublisher(migrationResults, publisher, registeredPublishers, asyncRegisteredPublishers);
        }
        List<KeyValue<MigrationResultsPublisher, Mono<Void>>> resultPublishers = registeredPublishers.build()
                .sorted(PUBLISHER_PRIORITY)
                .collect(Collectors.toCollection(LinkedList::new));
        Stream<KeyValue<MigrationResultsPublisher, Mono<Void>>> asyncResultPublishers = asyncRegisteredPublishers
                .build()
                .sorted(PUBLISHER_PRIORITY);
        Flux<Boolean> publishedResults = Mono.just(resultPublishers)
                .subscribeOn(executorScheduler)
                .flatMapMany(Flux::fromIterable)
                .flatMap(this::publishResults);
        Flux<Boolean> asyncPublishedResults = Flux.fromStream(asyncResultPublishers)
                .subscribeOn(executorScheduler)
                .flatMap(this::publishResults);
        return Flux.concat(asyncPublishedResults, publishedResults);
    }

    private Mono<Boolean> publishResults(KeyValue<MigrationResultsPublisher, Mono<Void>> registeredPublisher) {
        MigrationResultsPublisher publisher = registeredPublisher.getKey();
        return registeredPublisher.getValue()
                .then(Mono.just(true))
                .doOnNext(isSuccess -> onResultsPublished(publisher))
                .onErrorResume(e -> onResultPublishException(e, publisher));
    }

    private Mono<Void> onPublishedResults(List<Boolean> consumptionResults,
                                          MigrationResults migrationResults,
                                          DataSource dataSource) {
        ongoingMigrations.remove(dataSource);
        log.info(
                "Liquibase migration: Success rate: [{}/{}] Failed: {} Skipped: {}. Timestamp: {}",
                migrationResults.getSuccessful(),
                migrationResults.getTotal(),
                migrationResults.getFailedResults(),
                migrationResults.getSkipped(),
                timeUtils.writeTimestamp(timeUtils.getTimestamp())
        );
        return Mono.empty();
    }

    private void onMigrationFinished(MigrationResult migrationResult, ChangeLog changeLog) {
        log.info(
                "Liquibase migration changelog: '{}' finished. Timestamp: '{}'. Successful: '{}'",
                changeLog.getPath(),
                timeUtils.writeTimestamp(migrationResult.getFinishTimestamp()),
                migrationResult.isSuccessful()
        );
    }

    private void onRegisterPublisher(MigrationResults migrationResults,
                                     MigrationResultsPublisher publisher,
                                     Stream.Builder<KeyValue<MigrationResultsPublisher, Mono<Void>>> registeredPub,
                                     Stream.Builder<KeyValue<MigrationResultsPublisher, Mono<Void>>> registerAsyncPub) {
        Mono<Void> resultPublisher;
        Stream.Builder<KeyValue<MigrationResultsPublisher, Mono<Void>>> publishers;
        if (publisher.isExecuteAsync()) {
            resultPublisher = Mono.fromRunnable(() -> publisher.getConsumer().accept(migrationResults))
                    .subscribeOn(executorScheduler)
                    .then();
            publishers = registeredPub;
        } else {
            resultPublisher = Mono.fromRunnable(() -> publisher.getConsumer().accept(migrationResults))
                    .then();
            publishers = registerAsyncPub;
        }
        publishers.add(new DefaultMapEntry<>(publisher, resultPublisher));
    }

    private void onPublishResults(MigrationResults migrationResults,
                                  String operationName) {
        log.debug("Liquibase migration: Publishing results for operation: '{}'", operationName);
    }

    private void onResultsPublished(MigrationResultsPublisher publisher) {
        log.error("Liquibase migration results publisher: '{}'. Notified observable", publisher.getOperationName());
        publisher.getPublishedObservable().emitValue(true, FAIL_FAST);
    }

    private MigrationResult onLiquibaseCloseException(Throwable e,
                                                      MigrationResult migrationResult) {
        log.error("Failed to close liquibase, reason:", e);
        return migrationResult;
    }

    private MigrationResults onConnectionException(Throwable e,
                                                   MigrationResults migrationResults) {
        log.error("Failed to close Jdbc connection, reason:", e);
        return migrationResults;
    }

    private Mono<Boolean> onResultPublishException(Throwable e, MigrationResultsPublisher publisher) {
        log.error(
                "Liquibase migration results publisher: '{}'. Notified observable with thrown error:",
                publisher.getOperationName(),
                e
        );
        publisher.getPublishedObservable().emitError(e, FAIL_FAST);
        return Mono.just(false);
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
