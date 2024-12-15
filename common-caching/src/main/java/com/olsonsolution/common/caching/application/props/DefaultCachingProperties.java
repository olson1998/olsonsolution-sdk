package com.olsonsolution.common.caching.application.props;

import com.olsonsolution.common.caching.domain.port.props.CachingProperties;
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

}
