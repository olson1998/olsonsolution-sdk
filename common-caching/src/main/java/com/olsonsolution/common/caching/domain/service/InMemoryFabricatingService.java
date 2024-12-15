package com.olsonsolution.common.caching.domain.service;

import com.github.benmanes.caffeine.cache.*;
import com.olsonsolution.common.caching.domain.model.exception.CachingConfigurationException;
import com.olsonsolution.common.caching.domain.port.props.CachingProperties;
import com.olsonsolution.common.caching.domain.port.repository.InMemoryCacheFactory;
import com.olsonsolution.common.spring.configurer.domain.port.repository.BeanLocator;
import com.olsonsolution.common.spring.configurer.domain.port.stereotype.BeanDefinition;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
public class InMemoryFabricatingService implements InMemoryCacheFactory {

    private final BeanLocator beanLocator;

    @Override
    public <K, V> Cache<K, V> fabricate(CachingProperties cachingProperties) {
        Caffeine<K, V> caffeine = fabricateCaffeine(cachingProperties);
        return caffeine.build();
    }

    @Override
    public <K, V> AsyncCache<K, V> fabricateAsync(CachingProperties cachingProperties) {
        Caffeine<K, V> caffeine = fabricateCaffeine(cachingProperties);
        return caffeine.buildAsync();
    }

    private <K, V> Caffeine<K, V> fabricateCaffeine(CachingProperties cachingProperties) {
        Caffeine<K, V> caffeine = (Caffeine<K, V>) Caffeine.newBuilder();
        caffeine.initialCapacity(cachingProperties.getInitialCapacity())
                .maximumSize(cachingProperties.getMaximumSize())
                .maximumWeight(cachingProperties.getMaximumWeight())
                .expireAfterAccess(cachingProperties.getExpireAfterAccess())
                .expireAfterWrite(cachingProperties.getExpireAfterWrite());
        registerCaffeineBean(cachingProperties.getExecutorProperties(), caffeine, Executor.class, Caffeine::executor);
        registerCaffeineBean(cachingProperties.getWeigherProperties(), caffeine, Weigher.class, Caffeine::weigher);
        registerCaffeineBean(cachingProperties.getRemovalListenerProperties(), caffeine, RemovalListener.class, Caffeine::removalListener);
        return caffeine;
    }

    private <K, V, B> void registerCaffeineBean(BeanDefinition beanDefinition,
                                                Caffeine<K, V> caffeine,
                                                Class<B> beanClass,
                                                BiConsumer<Caffeine<K, V>, B> caffeineRegister) {
        Class<?> javaClass = beanDefinition.getJavaClass();
        if(javaClass == null) {
            return;
        }
        if(beanClass.isAssignableFrom(javaClass)) {
            B bean = beanLocator.getBean(beanDefinition, beanClass);
            caffeineRegister.accept(caffeine, bean);
        } else {
            String msg = "Bean of type %s is not assignable from type %s"
                    .formatted(beanClass.getCanonicalName(), javaClass);
            throw new CachingConfigurationException(msg);
        }
    }

}
