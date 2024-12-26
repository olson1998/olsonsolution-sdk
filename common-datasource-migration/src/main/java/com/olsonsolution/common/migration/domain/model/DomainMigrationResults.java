package com.olsonsolution.common.migration.domain.model;

import com.olsonsolution.common.migration.domain.port.stereotype.ChangeLog;
import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResult;
import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResults;
import lombok.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@Getter
@ToString
@EqualsAndHashCode
public class DomainMigrationResults implements MigrationResults {

    private final int successful;

    private final int failed;

    private final int skipped;

    private final Collection<? extends MigrationResult> successfulResults;

    private final Collection<? extends MigrationResult> failedResults;

    private final Collection<? extends MigrationResult> skippedResults;

    public DomainMigrationResults(Collection<? extends MigrationResult> successfulResults,
                                  Collection<? extends MigrationResult> failedResults,
                                  Collection<? extends MigrationResult> skippedResults) {
        this.successfulResults = Objects.requireNonNullElseGet(successfulResults, Collections::emptyList);
        this.failedResults = Objects.requireNonNullElseGet(failedResults, Collections::emptyList);
        this.skippedResults = Objects.requireNonNullElseGet(skippedResults, Collections::emptyList);
        this.successful = successfulResults.size();
        this.failed = failedResults.size();
        this.skipped = skippedResults.size();
    }

    public static MigrationResults empty() {
        return new DomainMigrationResults(null, null, null);
    }

}
