package com.olsonsolution.messagebus.domain.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.olsonsolution.messagebus.domain.port.stereotype.SubscriptionDefinition;
import com.olsonsolution.messagebus.domain.port.stereotype.SubscriptionMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.TopicPartition;
import org.joda.time.MutableDateTime;

import java.security.PublicKey;
import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DomainSubscriptionMetadata implements SubscriptionMetadata {

    private String publisher;

    private String subscriber;

    @JsonDeserialize(as = DomainSubscriptionDefinition.class)
    private SubscriptionDefinition definition;

    private MutableDateTime createdTime;

    private MutableDateTime expire;

    private Collection<TopicPartition> assignment;

    private PublicKey publicKey;

}
