package com.olsonsolution.common.migration.domain.service;

import com.olsonsolution.common.migration.domain.port.repository.MigrationResultsPublisher;
import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResults;
import lombok.*;
import org.joda.time.MutableDateTime;
import reactor.core.publisher.Sinks;

import java.util.function.Consumer;

@Getter
@ToString

@Builder
@RequiredArgsConstructor
public class DomainMigrationResultsPublisher implements MigrationResultsPublisher {

    @Setter
    private MutableDateTime subscriptionTimestamp;

    private final boolean executeAsync;

    private final int priority;

    @NonNull
    private final String operationName;

    @NonNull
    @ToString.Exclude
    private final Consumer<MigrationResults> consumer;

    @NonNull
    @Builder.Default
    @ToString.Exclude
    private final Sinks.One<Boolean> publishedObservable = Sinks.one();

}
