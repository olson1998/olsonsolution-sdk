package com.olsonsolution.common.spring.domain.service.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import com.olsonsolution.common.spring.domain.service.async.AbstractThreadLocalAware;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;

@RequiredArgsConstructor
public class DataSourceSpecificationManagingService extends AbstractThreadLocalAware<DataSourceSpec>
        implements DataSourceSpecManager {

    @Override
    public SqlPermission getThreadLocalPermission() {
        DataSourceSpec dataSourceSpec = getThreadLocal();
        if (dataSourceSpec != null) {
            return dataSourceSpec.getPermission();
        } else {
            return null;
        }
    }

    @Override
    public DataSourceSpec resolveCurrentTenantIdentifier() {
        return getThreadLocal();
    }

    @Override
    public @UnknownKeyFor @NonNull @Initialized boolean validateExistingCurrentSessions() {
        return false;
    }
}
