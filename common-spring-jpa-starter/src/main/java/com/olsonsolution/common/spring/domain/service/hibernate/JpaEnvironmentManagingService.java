package com.olsonsolution.common.spring.domain.service.hibernate;

import com.olsonsolution.common.spring.domain.port.repository.hibernate.JpaEnvironmentManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingEntityManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingEntityManagerFactory;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingPlatformTransactionManager;
import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;

@Slf4j
@RequiredArgsConstructor
public class JpaEnvironmentManagingService implements JpaEnvironmentManager {

    private final RoutingEntityManager routingEntityManager;

    private final RoutingEntityManagerFactory routingEntityManagerFactory;

    private final RoutingPlatformTransactionManager routingPlatformTransactionManager;

    private final ThreadLocal<JpaEnvironment> currentJpaEnvironment = new ThreadLocal<>();

    @Override
    public void setCurrent(JpaEnvironment jpaEnvironment) {
        currentJpaEnvironment.set(jpaEnvironment);
        routingEntityManagerFactory.setEnvironment(jpaEnvironment);
        routingEntityManager.setEnvironment(jpaEnvironment);
        routingPlatformTransactionManager.setEnvironment(jpaEnvironment);
        log.info("Jpa environment {}", jpaEnvironment);
    }

    @Override
    public void clear() {
        currentJpaEnvironment.remove();
        routingPlatformTransactionManager.clear();
        routingEntityManager.clear();
        routingEntityManagerFactory.clear();
    }

    @Override
    public JpaEnvironment resolveCurrentTenantIdentifier() {
        return currentJpaEnvironment.get();
    }

    @Override
    public @UnknownKeyFor @NonNull @Initialized boolean validateExistingCurrentSessions() {
        return false;
    }
}
