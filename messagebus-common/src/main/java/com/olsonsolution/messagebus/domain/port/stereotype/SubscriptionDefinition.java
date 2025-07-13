package com.olsonsolution.messagebus.domain.port.stereotype;

public interface SubscriptionDefinition {

    SubscriptionType getType();

    String getDocument();

}
