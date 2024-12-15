package com.olsonsolution.common.caching.domain.port.repository;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.olsonsolution.common.caching.domain.port.props.CachingProperties;

public interface InMemoryCacheFactory {

    <K, V> Cache<K, V> fabricate(CachingProperties cachingProperties);

    <K, V> AsyncCache<K, V> fabricateAsync(CachingProperties cachingProperties);
}
