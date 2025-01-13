package com.olsonsolution.common.migration.domain.port.repository;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;

import javax.sql.DataSource;
import java.util.concurrent.CompletableFuture;

public interface MigrationService {

    Boolean migrate(DataSource dataSource, SqlVendor sqlVendor, MigrationResultsPublisher publisher);

    CompletableFuture<Boolean> migrateAsync(DataSource dataSource,
                                            SqlVendor sqlVendor,
                                            MigrationResultsPublisher publisher);

}
