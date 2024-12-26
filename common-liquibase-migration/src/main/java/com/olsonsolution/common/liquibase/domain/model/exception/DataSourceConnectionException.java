package com.olsonsolution.common.liquibase.domain.model.exception;

import com.olsonsolution.common.migration.domain.port.stereotype.ChangeLog;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.ChangeLogMigrationException;
import lombok.Getter;

public class DataSourceConnectionException extends ChangeLogMigrationException {

    @Getter
    private final ChangeLog changeLog;

    public DataSourceConnectionException(ConnectionFailedException cause, ChangeLog changeLog) {
        super(cause);
        this.changeLog = changeLog;
    }
}
