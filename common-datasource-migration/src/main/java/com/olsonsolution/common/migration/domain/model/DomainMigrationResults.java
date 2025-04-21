package com.olsonsolution.common.migration.domain.model;

import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResult;
import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResults;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.ChangeLogMigrationException;
import com.olsonsolution.common.migration.domain.port.stereotype.exception.ChangeLogSkippedException;
import lombok.*;
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
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DomainMigrationResults implements MigrationResults {

    private final int successful;

    private final int failed;

    private final int skipped;

    private final int total;

    private final ArrayList<MigrationResult> successfulResults;

    private final HashMap<MigrationResult, ChangeLogMigrationException> failedResults;

    private final HashMap<MigrationResult, ChangeLogSkippedException> skippedResults;

    public static MigrationResults fromResults(Collection<MigrationResult> results) {
        ArrayList<MigrationResult> successfulResults = results.stream()
                .filter(MigrationResult::isSuccessful)
                .collect(Collectors.toCollection(ArrayList::new));
        HashMap<MigrationResult, ChangeLogMigrationException> failedResults =
                mapCause(results, MigrationResult::isFailed, MigrationResult::findFailureCause);
        HashMap<MigrationResult, ChangeLogSkippedException> skippedResults =
                mapCause(results, MigrationResult::isSkipped, MigrationResult::findSkippingCause);
        int totalQty = results.size();
        int failedQty = failedResults.size();
        int skippedQty = skippedResults.size();
        int successfulQty = successfulResults.size();
        return new DomainMigrationResults(
                successfulQty,
                failedQty,
                skippedQty,
                totalQty,
                successfulResults,
                failedResults,
                skippedResults
        );
    }

    public static MigrationResults empty() {
        return new DomainMigrationResults(
                0,
                0,
                0,
                0,
                new ArrayList<>(0),
                new HashMap<>(0),
                new HashMap<>(0)
        );
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
