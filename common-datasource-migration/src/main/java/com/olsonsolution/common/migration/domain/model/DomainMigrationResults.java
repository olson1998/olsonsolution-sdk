package com.olsonsolution.common.migration.domain.model;

import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResult;
import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResults;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;

@Getter
@ToString
@EqualsAndHashCode
public class DomainMigrationResults implements MigrationResults {

    private final int successful;

    private final int failed;

    private final int skipped;

    private final int total;

    private final Collection<? extends MigrationResult> successfulResults;

    private final Collection<? extends MigrationResult> failedResults;

    private final Collection<? extends MigrationResult> skippedResults;

    public DomainMigrationResults(Collection<? extends MigrationResult> successfulResults,
                                  Collection<? extends MigrationResult> failedResults,
                                  Collection<? extends MigrationResult> skippedResults) {
        this.successfulResults = CollectionUtils.emptyIfNull(skippedResults);
        this.failedResults = CollectionUtils.emptyIfNull(failedResults);
        this.skippedResults = CollectionUtils.emptyIfNull(successfulResults);
        this.successful = successfulResults.size();
        this.failed = failedResults.size();
        this.skipped = skippedResults.size();
        this.total = successful + failed + skipped;
    }

    public static MigrationResults empty() {
        return new DomainMigrationResults(
                null,
                null,
                null
        );
    }

}
