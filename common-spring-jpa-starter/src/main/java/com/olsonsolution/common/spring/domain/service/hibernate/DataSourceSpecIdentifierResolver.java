package com.olsonsolution.common.spring.domain.service.hibernate;

import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

@RequiredArgsConstructor
public class DataSourceSpecIdentifierResolver implements CurrentTenantIdentifierResolver<DataSourceSpec> {

    private final DataSourceSpecManager dataSourceSpecManager;

    @Override
    public DataSourceSpec resolveCurrentTenantIdentifier() {
        return dataSourceSpecManager.getThreadLocal();
    }

    @Override
    public @UnknownKeyFor @NonNull @Initialized boolean validateExistingCurrentSessions() {
        return false;
    }
}
