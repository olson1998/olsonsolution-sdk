package com.olsonsolution.common.spring.domain.port.stereotype.jpa;

import com.olsonsolution.common.spring.domain.port.stereotype.datasource.RoutingDataSource;

public interface JpaEnvironment {

    Class<?> getDialect();

    RoutingDataSource getDataBaseEnvironment();

}
