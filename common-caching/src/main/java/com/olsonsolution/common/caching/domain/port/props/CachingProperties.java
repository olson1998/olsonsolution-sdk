package com.olsonsolution.common.caching.domain.port.props;

import java.time.Duration;

public interface CachingProperties {

    int getInitialCapacity();

    long getMaximumSize();

    Long getMaximumWeight();

    Duration getExpireAfterAccess();

    Duration getExpireAfterWrite();

}
