package com.olsonsolution.common.spring.domain.port.props.jpa;

import java.time.Duration;

public interface RoutingDataSourceProperties {

    int getMaxDataSources();

    Duration getExpireTimeout();

}
