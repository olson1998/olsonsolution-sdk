package com.olsonsolution.common.caching.domain.port.props;

import java.time.Duration;

public interface CachingProperties {

    int getInitialCapacity();

    Long getMaximumSize();

    Long getMaximumWeight();

    Duration getExpireAfterAccess();

    Duration getExpireAfterWrite();

}
