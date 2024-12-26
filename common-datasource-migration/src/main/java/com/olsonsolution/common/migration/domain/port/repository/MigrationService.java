package com.olsonsolution.common.migration.domain.port.repository;

import com.olsonsolution.common.migration.domain.port.stereotype.ChangeLog;
import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResults;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface MigrationService {

    void migrate(DataSource dataSource, Collection<? extends ChangeLog> changeLogs);

    void migrate(DataSource dataSource,
                 Collection<? extends ChangeLog> changeLogs,
                 Consumer<? extends MigrationResults> migrationResultsConsumer);

    CompletableFuture<Void> migrateAsync(DataSource dataSource, Collection<? extends ChangeLog> changeLogs);

    CompletableFuture<Void> migrateAsync(DataSource dataSource,
                                         Collection<? extends ChangeLog> changeLogs,
                                         Consumer<? extends MigrationResults> migrationResultsConsumer);

}
