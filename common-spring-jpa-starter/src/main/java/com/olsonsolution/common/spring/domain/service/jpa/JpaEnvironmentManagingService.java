package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaEnvironmentManager;
import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JpaEnvironmentManagingService implements JpaEnvironmentManager {

    private final ThreadLocal<JpaEnvironment> jpaEnvironmentThreadLocal = new ThreadLocal<>();

    @Override
    public JpaEnvironment getThreadLocal() {
        return jpaEnvironmentThreadLocal.get();
    }

    @Override
    public void setCurrent(JpaEnvironment jpaEnvironment) {
        jpaEnvironmentThreadLocal.set(jpaEnvironment);
        log.info("Jpa environment {}", jpaEnvironment);
    }

    @Override
    public void clear() {
        jpaEnvironmentThreadLocal.remove();
    }

}
