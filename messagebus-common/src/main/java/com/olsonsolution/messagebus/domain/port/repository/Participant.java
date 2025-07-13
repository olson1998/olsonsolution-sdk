package com.olsonsolution.messagebus.domain.port.repository;

import com.olsonsolution.messagebus.domain.port.stereotype.SubscriptionDefinition;

import java.util.Collection;
import java.util.List;

public interface Participant<S extends Subscription<R>, R> extends AutoCloseable {

    List<S> getSubscriptions();

    void subscribe(SubscriptionDefinition definition);

    void subscribe(Collection<SubscriptionDefinition> definitions);

    void unsubscribe(SubscriptionDefinition definition);

    void unsubscribe(Collection<SubscriptionDefinition> definitions);

}
