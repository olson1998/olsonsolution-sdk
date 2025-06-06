package com.olsonsolution.common.spring.domain.port.repository.datasource;

import com.github.benmanes.caffeine.cache.RemovalListener;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.JpaDataSourceSpec;

import javax.sql.DataSource;

public interface DataSourceEvictor extends RemovalListener<JpaDataSourceSpec, DataSource> {
}
