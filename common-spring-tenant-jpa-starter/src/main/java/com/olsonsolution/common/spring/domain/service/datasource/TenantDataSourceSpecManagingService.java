package com.olsonsolution.common.spring.domain.service.datasource;

import com.olsonsolution.common.data.domain.model.sql.SqlPermissions;
import com.olsonsolution.common.spring.domain.model.datasource.DomainDataSourceSpec;
import com.olsonsolution.common.spring.domain.port.repository.context.LocalContextManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.TenantDataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import com.olsonsolution.common.spring.domain.port.sterotype.context.TenantContext;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class TenantDataSourceSpecManagingService implements TenantDataSourceSpecManager {

    private final LocalContextManager localContextManager;

    @Override
    public DataSourceSpec getThreadLocal() {
        return obtainThreadLocal().orElse(null);
    }

    @Override
    public Optional<DataSourceSpec> obtainThreadLocal() {
        return localContextManager.obtainThreadLocalAs(TenantContext.class)
                .map(this::fromTenantContext);
    }

    @Override
    public void setThreadLocal(DataSourceSpec value) {

    }

    @Override
    public void clear() {

    }

    private DataSourceSpec fromTenantContext(TenantContext tenantContext) {
        return DomainDataSourceSpec.builder()
                .dataSourceName(tenantContext.getTenant().getId() + "_DataSource")
                .permission(SqlPermissions.RWX)
                .build();
    }

}
