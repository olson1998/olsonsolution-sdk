package com.olsonsolution.common.spring.domain.service.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpecification;
import com.olsonsolution.common.spring.domain.service.async.AbstractThreadLocalAware;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;

@RequiredArgsConstructor
public class DataSourceSpecificationManagingService extends AbstractThreadLocalAware<DataSourceSpecification>
        implements DataSourceSpecManager {

    @Override
    public SqlPermission getThreadLocalPermission() {
        DataSourceSpecification dataSourceSpecification = getThreadLocal();
        if (dataSourceSpecification != null) {
            return dataSourceSpecification.getPermission();
        } else {
            return null;
        }
    }

    @Override
    public DataSourceSpecification resolveCurrentTenantIdentifier() {
        return getThreadLocal();
    }

    @Override
    public @UnknownKeyFor @NonNull @Initialized boolean validateExistingCurrentSessions() {
        return false;
    }
}
