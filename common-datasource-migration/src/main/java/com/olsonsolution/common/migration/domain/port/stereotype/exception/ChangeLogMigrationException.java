package com.olsonsolution.common.migration.domain.port.stereotype.exception;

import com.olsonsolution.common.migration.domain.port.stereotype.ChangeLog;

public abstract class ChangeLogMigrationException extends Exception {

    public abstract ChangeLog getChangeLog();

    protected ChangeLogMigrationException() {
    }

    protected ChangeLogMigrationException(String message) {
        super(message);
    }

    protected ChangeLogMigrationException(Throwable cause) {
        super(cause);
    }
}
