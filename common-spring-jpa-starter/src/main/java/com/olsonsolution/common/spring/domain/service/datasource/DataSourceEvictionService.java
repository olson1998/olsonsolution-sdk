package com.olsonsolution.common.spring.domain.service.datasource;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DataSourceEvictor;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.JpaDataSourceSpec;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.sql.DataSource;

@Slf4j
public class DataSourceEvictionService implements DataSourceEvictor {

    @Override
    public void onRemoval(@Nullable JpaDataSourceSpec jpaDataSourceSpec,
                          @Nullable DataSource dataSource,
                          RemovalCause removalCause) {
        if (dataSource == null) {
            return;
        }
        try {
            if (dataSource instanceof AutoCloseable closeable) {
                closeable.close();
            }
        } catch (Exception e) {
            log.error("Data source has not been closed properly, reason:", e);
        }
        log.info("Data source {} evicted cause: {}", jpaDataSourceSpec, removalCause);
    }
}
