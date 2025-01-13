package com.olsonsolution.common.migration.domain.model;

import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResult;
import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResults;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.ChangeLogMigrationException;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.ChangeLogSkippedException;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.MigrationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@ToString
@EqualsAndHashCode
public class DomainMigrationResults implements MigrationResults {

    private final int successful;

    private final int failed;

    private final int skipped;

    private final int total;

    private final ArrayList<? extends MigrationResult> successfulResults;

    private final HashMap<MigrationResult, ChangeLogMigrationException> failedResults;

    private final HashMap<MigrationResult, ChangeLogSkippedException> skippedResults;

    public DomainMigrationResults(Collection<? extends MigrationResult> results) {
        this.successfulResults = streamResults(results, MigrationResult::isSuccessful)
                .collect(Collectors.toCollection(ArrayList::new));
        this.failedResults = mapCause(results, MigrationResult::isFailed, MigrationResult::findFailureCause);
        this.skippedResults = mapCause(results, MigrationResult::isSkipped, MigrationResult::findSkippingCause);
        this.successful = successfulResults.size();
        this.failed = failedResults.size();
        this.skipped = skippedResults.size();
        this.total = successful + failed + skipped;
    }

    public static MigrationResults empty() {
        return new DomainMigrationResults(null);
    }

    private static Stream<? extends MigrationResult> streamResults(Collection<? extends MigrationResult> results,
                                                                   Predicate<MigrationResult> predicate) {
        if (results == null) {
            return Stream.empty();
        } else {
            return results.stream()
                    .filter(predicate);
        }
    }

    private static <E extends Exception> HashMap<MigrationResult, E> mapCause(
            Collection<? extends MigrationResult> results,
            Predicate<MigrationResult> predicate,
            Function<MigrationResult, Optional<? extends E>> errorCauseMapper) {
        return streamResults(results, predicate)
                .map(result -> errorCauseMapper.apply(result)
                        .map(cause -> new DefaultMapEntry<>(result, cause)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(KeyValue::getKey, KeyValue::getValue),
                        HashMap::new
                ));
    }

}
