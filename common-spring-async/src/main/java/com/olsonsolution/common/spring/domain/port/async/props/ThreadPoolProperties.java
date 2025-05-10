package com.olsonsolution.common.spring.domain.port.async.props;

import java.time.Duration;

public interface ThreadPoolProperties {

    int getSize();

    int getMaxPoolSize();

    int getQueueCapacity();

    Duration getKeepAlive();

}
