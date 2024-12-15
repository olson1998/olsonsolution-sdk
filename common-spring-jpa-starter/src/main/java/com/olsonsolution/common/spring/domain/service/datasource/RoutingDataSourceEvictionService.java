package com.olsonsolution.common.spring.domain.service.datasource;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.olsonsolution.common.spring.domain.port.repository.datasource.RoutingDataSourceEvictor;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.RoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

@Slf4j
public class RoutingDataSourceEvictionService implements RoutingDataSourceEvictor {

    @Override
    public void onRemoval(@Nullable RoutingDataSource routingDataSource, @Nullable HikariDataSource hikariDataSource, RemovalCause removalCause) {
        Optional.ofNullable(hikariDataSource).ifPresent(dataSource -> {
            log.info("Closing connection to {} cause: {}", routingDataSource, removalCause);
            dataSource.close();
        });
    }
}
