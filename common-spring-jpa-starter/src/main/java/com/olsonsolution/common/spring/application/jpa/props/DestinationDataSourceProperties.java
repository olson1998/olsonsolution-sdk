package com.olsonsolution.common.spring.application.jpa.props;

import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.RoutingDataSource;
import com.zaxxer.hikari.HikariConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
@ConfigurationProperties(prefix = "common.spring.application.jpa.routing-data-source")
public class DestinationDataSourceProperties implements DestinationDataSourceProvider {

    private final Map<String, HikariConfig> instance = new HashMap<>();

    @Override
    public RoutingDataSource getProductDataSourceEnvironment() {
        return null;
    }

    @Override
    public Optional<HikariConfig> findDestinationConfig(RoutingDataSource routingDataSource) {
        return Optional.ofNullable(instance.get(routingDataSource.getId()))
                .filter(hikariConfig -> isMatchingDataSource(hikariConfig, routingDataSource));
    }

    private boolean isMatchingDataSource(HikariConfig hikariConfig, RoutingDataSource routingDataSource) {
        return StringUtils.equals(hikariConfig.getCatalog(), routingDataSource.getDataBase()) &&
                StringUtils.equals(hikariConfig.getSchema(), routingDataSource.getSchema());
    }

}
