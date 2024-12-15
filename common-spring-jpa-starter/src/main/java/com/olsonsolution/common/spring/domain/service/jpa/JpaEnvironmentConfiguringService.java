package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaEnvironmentConfigurer;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingEntityManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingEntityManagerFactory;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingPlatformTransactionManager;
import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JpaEnvironmentConfiguringService implements JpaEnvironmentConfigurer {

    private final RoutingEntityManager routingEntityManager;

    private final RoutingEntityManagerFactory routingEntityManagerFactory;

    private final RoutingPlatformTransactionManager routingPlatformTransactionManager;

    @Override
    public void configure(JpaEnvironment jpaEnvironment) {
        routingEntityManagerFactory.setEnvironment(jpaEnvironment);
        routingEntityManager.setEnvironment(jpaEnvironment);
        routingPlatformTransactionManager.setEnvironment(jpaEnvironment);
    }

    @Override
    public void clear() {
        routingPlatformTransactionManager.clear();
        routingEntityManager.clear();
        routingEntityManagerFactory.clear();
    }
}
