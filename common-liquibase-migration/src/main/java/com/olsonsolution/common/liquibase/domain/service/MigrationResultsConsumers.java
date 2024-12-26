package com.olsonsolution.common.liquibase.domain.service;

import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResults;

import java.util.LinkedList;
import java.util.function.Consumer;

class MigrationResultsConsumers extends LinkedList<Consumer<MigrationResults>> {

    private boolean publishingLock;

    @Override
    public boolean add(Consumer<MigrationResults> migrationResultsConsumer) {
        if (publishingLock) {
            return false;
        } else {
            return super.add(migrationResultsConsumer);
        }
    }

    @Override
    public synchronized void forEach(Consumer<? super Consumer<MigrationResults>> action) {
        publishingLock = true;
        super.forEach(action);
        clear();
        publishingLock = false;
    }

}
