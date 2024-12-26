package com.olsonsolution.common.migration.domain.port.stereotype;

import com.olsonsolution.common.migration.domain.port.stereotype.exception.ChangeLogMigrationException;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.ChangeLogSkippedException;
import org.joda.time.MutableDateTime;

import java.io.Serializable;
import java.util.Optional;

public interface MigrationResult extends Serializable {

    boolean isCreatedSchema();

    boolean isSuccessful();

    boolean isFailed();

    boolean isSkipped();

    MutableDateTime getStartTimestamp();

    MutableDateTime getFinishTimestamp();

    Optional<? extends ChangeLogSkippedException> findSkippingCause();

    Optional<? extends ChangeLogMigrationException> findFailureCause();

}
