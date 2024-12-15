package com.olsonsolution.common.caching.domain.service;

import com.github.benmanes.caffeine.cache.*;
import com.olsonsolution.common.caching.domain.port.props.CachingProperties;
import com.olsonsolution.common.caching.domain.port.repository.InMemoryCacheFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class InMemoryFabricatingService implements InMemoryCacheFactory {

    @Override
    public <K, V> Cache<K, V> fabricate(CachingProperties cachingProperties,
                                        Executor executor,
                                        Weigher<K, V> weigher,
                                        RemovalListener<K, V> removalListener) {
        Caffeine<K, V> caffeine = fabricateCaffeine(cachingProperties, executor, weigher, removalListener);
        return caffeine.build();
    }

    @Override
    public <K, V> AsyncCache<K, V> fabricateAsync(CachingProperties cachingProperties,
                                                  Executor executor,
                                                  Weigher<K, V> weigher,
                                                  RemovalListener<K, V> removalListener) {
        Caffeine<K, V> caffeine = fabricateCaffeine(cachingProperties, executor, weigher, removalListener);
        return caffeine.buildAsync();
    }

    private <K, V> Caffeine<K, V> fabricateCaffeine(CachingProperties cachingProperties,
                                                    Executor executor,
                                                    Weigher<K, V> weigher,
                                                    RemovalListener<K, V> removalListener) {
        Caffeine<K, V> caffeine = (Caffeine<K, V>) Caffeine.newBuilder();
        caffeine.initialCapacity(cachingProperties.getInitialCapacity())
                .maximumSize(cachingProperties.getMaximumSize())
                .maximumWeight(cachingProperties.getMaximumWeight())
                .expireAfterAccess(cachingProperties.getExpireAfterAccess())
                .expireAfterWrite(cachingProperties.getExpireAfterWrite());
        Optional.ofNullable(executor).ifPresent(caffeine::executor);
        Optional.ofNullable(weigher).ifPresent(caffeine::weigher);
        Optional.ofNullable(removalListener).ifPresent(caffeine::removalListener);
        return caffeine;
    }

}
