package com.olsonsolution.messagebus.domain.port.stereotype;

import org.apache.kafka.common.TopicPartition;
import org.joda.time.MutableDateTime;

import java.security.PublicKey;
import java.util.Collection;

public interface SubscriptionMetadata {

    String getPublisher();

    String getSubscriber();

    SubscriptionDefinition getDefinition();

    MutableDateTime getCreatedTime();

    MutableDateTime getExpire();

    Collection<TopicPartition> getAssignment();

    PublicKey getPublicKey();

}
