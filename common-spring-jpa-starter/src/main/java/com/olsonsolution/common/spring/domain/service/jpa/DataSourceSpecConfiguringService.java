package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecConfigurer;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingEntityManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingEntityManagerFactory;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingPlatformTransactionManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DataSourceSpecConfiguringService implements DataSourceSpecConfigurer {

    private final RoutingEntityManager routingEntityManager;

    private final RoutingEntityManagerFactory routingEntityManagerFactory;

    private final RoutingPlatformTransactionManager routingPlatformTransactionManager;

    @Override
    public void configure(DataSourceSpec dataSourceSpec) {
        routingEntityManagerFactory.setDataSourceSpec(dataSourceSpec);
        routingEntityManager.setDataSourceSpec(dataSourceSpec);
        routingPlatformTransactionManager.setDataSourceSpec(dataSourceSpec);
    }

    @Override
    public void clear() {
        routingPlatformTransactionManager.clear();
        routingEntityManager.clear();
        routingEntityManagerFactory.clear();
    }
}
