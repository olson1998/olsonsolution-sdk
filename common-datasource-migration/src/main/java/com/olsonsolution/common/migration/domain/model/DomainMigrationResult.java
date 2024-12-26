package com.olsonsolution.common.migration.domain.model;

import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResult;
import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResults;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.ChangeLogMigrationException;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.ChangeLogSkippedException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.MutableDateTime;

import java.util.Optional;

@Getter
@ToString
@EqualsAndHashCode
public class DomainMigrationResult implements MigrationResult {

    private final boolean createdSchema;

    private final ChangeLogSkippedException skippingCause;

    private final ChangeLogMigrationException failureCause;

    private final MutableDateTime startTimestamp;

    private final MutableDateTime finishTimestamp;

    @Builder(builderMethodName = "successfulResult")
    public DomainMigrationResult(boolean createdSchema,
                                 MutableDateTime startTimestamp,
                                 MutableDateTime finishTimestamp) {
        this.createdSchema = createdSchema;
        this.skippingCause = null;
        this.failureCause = null;
        this.startTimestamp = startTimestamp;
        this.finishTimestamp = finishTimestamp;
    }

    @Builder(builderMethodName = "skippedResult")
    public DomainMigrationResult(boolean createdSchema,
                                 ChangeLogSkippedException skippingCause,
                                 MutableDateTime startTimestamp,
                                 MutableDateTime finishTimestamp) {
        this.createdSchema = createdSchema;
        this.skippingCause = skippingCause;
        this.failureCause = null;
        this.startTimestamp = startTimestamp;
        this.finishTimestamp = finishTimestamp;
    }

    @Builder(builderMethodName = "failedResult")
    public DomainMigrationResult(boolean createdSchema,
                                 ChangeLogMigrationException failureCause,
                                 MutableDateTime startTimestamp,
                                 MutableDateTime finishTimestamp) {
        this.createdSchema = createdSchema;
        this.skippingCause = null;
        this.failureCause = failureCause;
        this.startTimestamp = startTimestamp;
        this.finishTimestamp = finishTimestamp;
    }

    @Override
    public boolean isSuccessful() {
        return ObjectUtils.allNotNull(skippingCause, failureCause);
    }

    @Override
    public boolean isFailed() {
        return failureCause != null;
    }

    @Override
    public boolean isSkipped() {
        return skippingCause != null;
    }

    @Override
    public Optional<? extends ChangeLogSkippedException> findSkippingCause() {
        return Optional.ofNullable(skippingCause);
    }

    @Override
    public Optional<? extends ChangeLogMigrationException> findFailureCause() {
        return Optional.ofNullable(failureCause);
    }
}
