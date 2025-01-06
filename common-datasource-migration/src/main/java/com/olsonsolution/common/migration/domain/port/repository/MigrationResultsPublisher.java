package com.olsonsolution.common.migration.domain.port.repository;

import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResults;
import lombok.NonNull;
import org.joda.time.MutableDateTime;
import reactor.core.publisher.Sinks;

import java.util.function.Consumer;

public interface MigrationResultsPublisher {

    int getPriority();

    @NonNull
    String getOperationName();

    boolean isExecuteAsync();

    MutableDateTime getSubscriptionTimestamp();

    @NonNull
    Consumer<MigrationResults> getConsumer();

    @NonNull
    Sinks.One<Boolean> getPublishedObservable();

    void setSubscriptionTimestamp(MutableDateTime timestamp);

}
