package com.olsonsolution.common.spring.domain.model.datasource;

import com.olsonsolution.common.spring.domain.port.stereotype.datasource.RoutingDataSource;
import com.zaxxer.hikari.HikariConfig;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class DefaultRoutingDataSource implements RoutingDataSource {

    private final String jdbcDriver;

    private final String host;

    private final Integer port;

    private final String dataBase;

    private final String schema;

    @Override
    public boolean isMatchingConfig(HikariConfig hikariConfig) {
        return StringUtils.equals(jdbcDriver, hikariConfig.getDataSourceClassName()) &&
                StringUtils.equals(host, hikariConfig.getHost())
    }
}
