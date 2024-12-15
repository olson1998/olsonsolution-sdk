package com.olsonsolution.common.spring.domain.port.repository.datasource;

import com.github.benmanes.caffeine.cache.RemovalListener;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.RoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;

public interface RoutingDataSourceEvictor extends RemovalListener<RoutingDataSource, HikariDataSource> {
}
