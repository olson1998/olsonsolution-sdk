package com.olsonsolution.common.spring.domain.service.datasource;

import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaSpecDataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.JpaDataSourceSpec;
import com.olsonsolution.common.spring.domain.service.async.AbstractThreadLocalAware;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;

@RequiredArgsConstructor
public class DataSourceSpecificationManagingService extends AbstractThreadLocalAware<JpaDataSourceSpec>
        implements JpaSpecDataSourceSpecManager {

    @Override
    public JpaDataSourceSpec resolveCurrentTenantIdentifier() {
        return getThreadLocal();
    }

    @Override
    public @UnknownKeyFor @NonNull @Initialized boolean validateExistingCurrentSessions() {
        return false;
    }
}
