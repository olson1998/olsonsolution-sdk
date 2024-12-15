package com.olsonsolution.common.caching.application.props;

import com.olsonsolution.common.caching.domain.port.props.CachingProperties;
import com.olsonsolution.common.spring.configurer.application.props.DefaultBeanDefinition;
import com.olsonsolution.common.spring.configurer.domain.port.stereotype.BeanDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;

@Data
@AllArgsConstructor
public class DefaultCachingProperties implements CachingProperties {

    private int initialCapacity;
    private long maximumSize;
    private Long maximumWeight;
    private Duration expireAfterAccess;
    private Duration expireAfterWrite;
    private DefaultBeanDefinition executor;
    private DefaultBeanDefinition removalListener;
    private DefaultBeanDefinition weigher;

    @Override
    public BeanDefinition getExecutorProperties() {
        return executor;
    }

    @Override
    public BeanDefinition getRemovalListenerProperties() {
        return removalListener;
    }

    @Override
    public BeanDefinition getWeigherProperties() {
        return weigher;
    }
}
