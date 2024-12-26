package com.olsonsolution.common.migration.domain.port.repository;

import com.olsonsolution.common.migration.domain.port.stereotype.ChangeLog;
import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResults;
import lombok.NonNull;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface MigrationService {

    void migrate(@NonNull DataSource dataSource, @NonNull Collection<? extends ChangeLog> changeLogs);

    void migrate(@NonNull DataSource dataSource,
                 @NonNull Collection<? extends ChangeLog> changeLogs,
                 Consumer<MigrationResults> resultsConsumer);

    CompletableFuture<Void> migrateAsync(@NonNull DataSource dataSource,
                                         @NonNull Collection<? extends ChangeLog> changeLogs);

    CompletableFuture<Void> migrateAsync(@NonNull DataSource dataSource,
                                         @NonNull Collection<? extends ChangeLog> changeLogs,
                                         Consumer<MigrationResults> resultsConsumer);

}
