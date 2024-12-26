package com.olsonsolution.common.liquibase.domain.model.exception;

import com.olsonsolution.common.migration.domain.port.stereotype.ChangeLog;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.MigrationException;
import lombok.Getter;
import org.joda.time.MutableDateTime;

import java.sql.SQLException;
import java.util.Collection;

@Getter
public class ConnectionFailedException extends MigrationException {

    private final Collection<? extends ChangeLog> changeLogs;

    private final MutableDateTime connectionAttemptTimestamp;

    private final MutableDateTime exceptionCreationTimestamp;

    public ConnectionFailedException(SQLException sqlException,
                                     MutableDateTime connectionAttemptTimestamp,
                                     Collection<? extends ChangeLog> changeLogs) {
        super(sqlException);
        this.changeLogs = changeLogs;
        this.connectionAttemptTimestamp = connectionAttemptTimestamp;
        this.exceptionCreationTimestamp = MutableDateTime.now();
    }
}
