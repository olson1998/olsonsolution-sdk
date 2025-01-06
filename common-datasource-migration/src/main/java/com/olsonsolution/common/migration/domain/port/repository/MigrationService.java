package com.olsonsolution.common.migration.domain.port.repository;

import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResults;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface MigrationService {

    Boolean migrate(DataSource dataSource, MigrationResultsPublisher publisher);

    CompletableFuture<Boolean> migrateAsync(DataSource dataSource, MigrationResultsPublisher publisher);

}
