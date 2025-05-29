package com.olsonsolution.common.spring.domain.port.repository.datasource;

import com.github.benmanes.caffeine.cache.RemovalListener;
import com.olsonsolution.common.data.domain.port.datasource.PermissionManagingDataSource;

public interface DataSourceEvictor extends RemovalListener<String, PermissionManagingDataSource> {
}
