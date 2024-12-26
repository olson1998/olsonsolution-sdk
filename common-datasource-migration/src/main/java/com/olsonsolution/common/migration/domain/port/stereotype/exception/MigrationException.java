package com.olsonsolution.common.migration.domain.port.stereotype.exception;

import com.olsonsolution.common.migration.domain.port.stereotype.ChangeLog;

import java.util.Collection;

public abstract class MigrationException extends Exception {

    public abstract Collection<? extends ChangeLog> getChangeLogs();

    protected MigrationException() {
    }

    protected MigrationException(Throwable cause) {
        super(cause);
    }

    protected MigrationException(String message) {
        super(message);
    }
}
