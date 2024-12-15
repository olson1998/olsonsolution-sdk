package com.olsonsolution.common.caching.domain.port.repository;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.cache.Weigher;
import com.olsonsolution.common.caching.domain.port.props.CachingProperties;

import java.util.concurrent.Executor;

public interface InMemoryCacheFactory {

    <K, V> Cache<K, V> fabricate(CachingProperties cachingProperties,
                                 Executor executor,
                                 Weigher<K, V> weigher,
                                 RemovalListener<K, V> removalListener);

    <K, V> AsyncCache<K, V> fabricateAsync(CachingProperties cachingProperties,
                                           Executor executor,
                                           Weigher<K, V> weigher,
                                           RemovalListener<K, V> removalListener);
}
