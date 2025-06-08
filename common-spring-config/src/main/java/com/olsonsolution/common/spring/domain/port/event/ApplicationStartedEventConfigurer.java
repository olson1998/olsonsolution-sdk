package com.olsonsolution.common.spring.domain.port.event;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

public interface ApplicationStartedEventConfigurer {

    @EventListener(ApplicationStartedEvent.class)
    void configure(ApplicationStartedEvent event);

}
