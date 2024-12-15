package com.olsonsolution.common.spring.domain.port.stereotype.datasource;

import com.zaxxer.hikari.HikariConfig;

public interface RoutingDataSource {

    Class<?> getJdbcDriver();

    String getHost();

    Integer getPort();

    String getDataBase();

    String getSchema();

    boolean isMatchingConfig(HikariConfig hikariConfig);

}
