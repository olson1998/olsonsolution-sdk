package com.olsonsolution.common.caching.domain.port.props;

import com.olsonsolution.common.spring.configurer.domain.port.stereotype.BeanDefinition;

import java.time.Duration;

public interface CachingProperties {

    int getInitialCapacity();

    long getMaximumSize();

    Long getMaximumWeight();

    Duration getExpireAfterAccess();

    Duration getExpireAfterWrite();

    BeanDefinition getExecutorProperties();

    BeanDefinition getRemovalListenerProperties();

    BeanDefinition getWeigherProperties();

}
