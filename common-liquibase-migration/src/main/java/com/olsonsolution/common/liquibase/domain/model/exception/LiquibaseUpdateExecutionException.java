package com.olsonsolution.common.liquibase.domain.model.exception;

import com.olsonsolution.common.migration.domain.port.stereotype.ChangeLog;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.ChangeLogMigrationException;
import liquibase.exception.LiquibaseException;
import lombok.Getter;

public class LiquibaseUpdateExecutionException extends ChangeLogMigrationException {

    @Getter
    private final ChangeLog changeLog;

    public LiquibaseUpdateExecutionException(LiquibaseException cause, ChangeLog changeLog) {
        super(cause);
        this.changeLog = changeLog;
    }
}
