package com.olsonsolution.common.spring.domain.port.repository.datasource;

import com.github.benmanes.caffeine.cache.RemovalListener;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.RoutingDataSource;

import javax.sql.DataSource;

public interface DataSourceEvictor extends RemovalListener<DataSourceSpec, DataSource> {
}
