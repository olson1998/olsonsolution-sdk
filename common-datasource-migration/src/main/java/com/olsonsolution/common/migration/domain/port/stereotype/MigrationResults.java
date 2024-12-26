package com.olsonsolution.common.migration.domain.port.stereotype;

import com.olsonsolution.common.migration.domain.port.stereotype.exception.ChangeLogSkippedException;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.MigrationException;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public interface MigrationResults extends Serializable {

    int getSuccessful();

    int getFailed();

    int getSkipped();

    @NonNull
    Collection<? extends MigrationResult> getSuccessfulResults();

    @NonNull
    Map<MigrationResult, MigrationException> getFailedResults();

    @NonNull
    Map<MigrationResult, ChangeLogSkippedException> getSkippedResults();

}
