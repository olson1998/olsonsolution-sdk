package com.olsonsolution.messagebus.domain.model;

import com.olsonsolution.messagebus.domain.port.stereotype.SubscriptionType;

public enum Subscriptions implements SubscriptionType {

    CONTINUOUS,
    ON_DEMAND,
    SINGLE

}
