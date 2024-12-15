package com.olsonsolution.common.spring.domain.service.datasource;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DataSourceEvictor;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;

@Slf4j
public class DataSourceEvictionService implements DataSourceEvictor {

    @Override
    public void onRemoval(@Nullable DataSourceSpec dataSourceSpec, @Nullable DataSource dataSource, RemovalCause removalCause) {
        if (dataSource instanceof Closeable closeableDataSource) {
            try {
                closeableDataSource.close();
                log.info("Closed data source id: '{}' data source: '{}'", dataSourceSpec, dataSource);
            } catch (IOException e) {
                log.info("Failed to close data source id: '{}' data source: '{}', reason:", dataSourceSpec, dataSource, e);
            }
        }
        log.info("Evicted data source id: '{}' data source specification: '{}' reason: {}", dataSourceSpec, dataSource, removalCause);
    }
}
