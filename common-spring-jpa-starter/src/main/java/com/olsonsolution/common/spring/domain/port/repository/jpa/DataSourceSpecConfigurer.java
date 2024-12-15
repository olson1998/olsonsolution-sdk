package com.olsonsolution.common.spring.domain.port.repository.jpa;

import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;

import java.util.Map;

public interface DataSourceSpecConfigurer {

    void register(Map<String, RoutingEntityManagerFactory> routingEntityManagerFactories,
                  Map<String, RoutingPlatformTransactionManager> routingPlatformTransactionManagers);

    void configure(DataSourceSpec dataSourceSpec);

    void clear();

}
