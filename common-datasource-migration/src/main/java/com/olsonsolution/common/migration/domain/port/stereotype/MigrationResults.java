package com.olsonsolution.common.migration.domain.port.stereotype;

import com.olsonsolution.common.migration.domain.port.stereotype.exception.ChangeLogMigrationException;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.ChangeLogSkippedException;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.MigrationException;
import lombok.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface MigrationResults extends Serializable {

    int getSuccessful();

    int getFailed();

    int getSkipped();

    int getTotal();

    @NonNull
    ArrayList<? extends MigrationResult> getSuccessfulResults();

    @NonNull
    HashMap<MigrationResult, ChangeLogMigrationException> getFailedResults();

    @NonNull
    HashMap<MigrationResult, ChangeLogSkippedException> getSkippedResults();

}
