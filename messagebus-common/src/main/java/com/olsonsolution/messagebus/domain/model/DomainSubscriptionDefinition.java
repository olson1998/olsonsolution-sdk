package com.olsonsolution.messagebus.domain.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.olsonsolution.messagebus.domain.port.stereotype.SubscriptionDefinition;
import com.olsonsolution.messagebus.domain.port.stereotype.SubscriptionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DomainSubscriptionDefinition implements SubscriptionDefinition {

    @JsonDeserialize(as = Subscriptions.class)
    private SubscriptionType type;

    private String document;

}
