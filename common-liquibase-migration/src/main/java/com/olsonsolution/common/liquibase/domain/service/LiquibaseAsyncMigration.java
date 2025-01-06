package com.olsonsolution.common.liquibase.domain.service;

import com.olsonsolution.common.migration.domain.port.repository.MigrationResultsPublisher;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

record LiquibaseAsyncMigration(CompletableFuture<Void> migration,
                               ConcurrentLinkedQueue<MigrationResultsPublisher> subscribingPublishers) {
}
