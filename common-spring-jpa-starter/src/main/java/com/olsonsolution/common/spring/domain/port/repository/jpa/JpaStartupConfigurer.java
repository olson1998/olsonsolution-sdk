package com.olsonsolution.common.spring.domain.port.repository.jpa;

import com.olsonsolution.common.spring.domain.port.event.ApplicationStartedEventConfigurer;

public interface JpaStartupConfigurer extends ApplicationStartedEventConfigurer {

    void configure();

}
