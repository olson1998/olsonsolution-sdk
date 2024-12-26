package com.olsonsolution.common.migration.domain.port.stereotype.exception;

import com.olsonsolution.common.migration.domain.port.stereotype.ChangeLog;

public abstract class MigrationException extends Exception{

    public abstract ChangeLog getChangeLog();

}
