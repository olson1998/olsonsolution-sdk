package com.olsonsolution.common.spring.domain.service.datasource;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.olsonsolution.common.data.domain.port.datasource.PermissionManagingDataSource;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DataSourceEvictor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.MutableDateTime;

@Slf4j
public class DataSourceEvictionService implements DataSourceEvictor {

    @Override
    public void onRemoval(@Nullable String dataSourceName,
                          @Nullable PermissionManagingDataSource permissionManagingDataSource,
                          RemovalCause removalCause) {
        if(permissionManagingDataSource == null) {
            return;
        }
        try {
            permissionManagingDataSource.close();
            log.info("Closed permission managing data source, name: '{}', cause: '{}'", dataSourceName, removalCause);
        } catch (Exception e) {
            log.error("Failed to close permission managing data source, name: '{}', reason:", dataSourceName, e);
        }
    }
}
