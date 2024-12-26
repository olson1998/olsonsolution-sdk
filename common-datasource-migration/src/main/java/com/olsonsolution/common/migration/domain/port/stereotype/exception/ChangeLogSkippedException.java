package com.olsonsolution.common.migration.domain.port.stereotype.exception;

import com.olsonsolution.common.migration.domain.port.stereotype.ChangeLog;

public abstract class ChangeLogSkippedException extends Exception {

    protected ChangeLogSkippedException() {
    }

    protected ChangeLogSkippedException(Throwable cause) {
        super(cause);
    }

    protected  ChangeLogSkippedException(String message) {
        super(message);
    }

    public abstract ChangeLog getChangeLog();

}
