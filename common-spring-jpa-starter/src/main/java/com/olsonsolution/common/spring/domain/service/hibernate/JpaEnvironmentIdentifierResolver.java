package com.olsonsolution.common.spring.domain.service.hibernate;

import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaEnvironmentManager;
import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

@RequiredArgsConstructor
public class JpaEnvironmentIdentifierResolver implements CurrentTenantIdentifierResolver<JpaEnvironment> {

    private final JpaEnvironmentManager jpaEnvironmentManager;

    @Override
    public JpaEnvironment resolveCurrentTenantIdentifier() {
        return jpaEnvironmentManager.getThreadLocal();
    }

    @Override
    public @UnknownKeyFor @NonNull @Initialized boolean validateExistingCurrentSessions() {
        return false;
    }
}
