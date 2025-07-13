package com.olsonsolution.messagebus.domain.port.repository;

import com.olsonsolution.messagebus.domain.port.stereotype.SubscriptionMetadata;
import com.olsonsolution.messagebus.domain.port.stereotype.exception.SubscriptionViolationException;

public interface Subscription<R> {

    void validate(R message) throws SubscriptionViolationException;

    SubscriptionMetadata getMetadata();

    void renew();

    void refresh();

}
