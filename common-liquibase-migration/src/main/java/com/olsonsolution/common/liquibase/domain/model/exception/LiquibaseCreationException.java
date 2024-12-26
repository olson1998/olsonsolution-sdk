package com.olsonsolution.common.liquibase.domain.model.exception;

import com.olsonsolution.common.migration.domain.port.stereotype.ChangeLog;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.ChangeLogSkippedException;
import liquibase.exception.LiquibaseException;
import lombok.Getter;
import org.joda.time.MutableDateTime;

@Getter
public class LiquibaseCreationException extends ChangeLogSkippedException {

    private final ChangeLog changeLog;

    private final MutableDateTime startTimestamp;

    public LiquibaseCreationException(LiquibaseException cause, ChangeLog changeLog, MutableDateTime startTimestamp) {
        super(cause);
        this.changeLog = changeLog;
        this.startTimestamp = startTimestamp;
    }
}
