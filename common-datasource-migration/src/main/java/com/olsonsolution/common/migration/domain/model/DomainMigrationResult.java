package com.olsonsolution.common.migration.domain.model;

import com.olsonsolution.common.migration.domain.port.stereotype.SchemaParseResult;
import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResult;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.ChangeLogMigrationException;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.ChangeLogSkippedException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.MutableDateTime;

import java.util.Map;
import java.util.Optional;

@Getter
@ToString
@EqualsAndHashCode
public class DomainMigrationResult implements MigrationResult {

    private final Map<String, SchemaParseResult> createdSchemas;

    private final ChangeLogSkippedException skippingCause;

    private final ChangeLogMigrationException failureCause;

    private final MutableDateTime startTimestamp;

    private final MutableDateTime finishTimestamp;

    @Builder(builderMethodName = "successfulResult", builderClassName = "SuccessfulResultsBuilder")
    public DomainMigrationResult(Map<String, SchemaParseResult> createdSchemas,
                                 MutableDateTime startTimestamp,
                                 MutableDateTime finishTimestamp) {
        this.createdSchemas = createdSchemas;
        this.skippingCause = null;
        this.failureCause = null;
        this.startTimestamp = startTimestamp;
        this.finishTimestamp = finishTimestamp;
    }

    @Builder(builderMethodName = "skippedResult", builderClassName = "SkippedResultsBuilder")
    public DomainMigrationResult(Map<String, SchemaParseResult> createdSchemas,
                                 ChangeLogSkippedException skippingCause,
                                 MutableDateTime startTimestamp,
                                 MutableDateTime finishTimestamp) {
        this.createdSchemas = createdSchemas;
        this.skippingCause = skippingCause;
        this.failureCause = null;
        this.startTimestamp = startTimestamp;
        this.finishTimestamp = finishTimestamp;
    }

    @Builder(builderMethodName = "failedResult", builderClassName = "FailedResultsBuilder")
    public DomainMigrationResult(Map<String, SchemaParseResult> createdSchemas,
                                 ChangeLogMigrationException failureCause,
                                 MutableDateTime startTimestamp,
                                 MutableDateTime finishTimestamp) {
        this.createdSchemas = createdSchemas;
        this.skippingCause = null;
        this.failureCause = failureCause;
        this.startTimestamp = startTimestamp;
        this.finishTimestamp = finishTimestamp;
    }

    @Override
    public boolean isSuccessful() {
        return !isFailed() && !isSkipped();
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
