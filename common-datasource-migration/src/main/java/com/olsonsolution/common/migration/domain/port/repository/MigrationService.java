package com.olsonsolution.common.migration.domain.port.repository;

import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResults;

import javax.sql.DataSource;
import java.util.concurrent.CompletableFuture;

public interface MigrationService {

    CompletableFuture<MigrationResults> migrateAsync(DataSource dataSource);

}
