package com.olsonsolution.common.spring.domain.port.repository.datasource;

import com.github.benmanes.caffeine.cache.RemovalListener;
import com.olsonsolution.common.data.domain.port.datasource.PermissionManagingDataSource;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;

import javax.sql.DataSource;

public interface DataSourceEvictor extends RemovalListener<String, PermissionManagingDataSource> {
}
