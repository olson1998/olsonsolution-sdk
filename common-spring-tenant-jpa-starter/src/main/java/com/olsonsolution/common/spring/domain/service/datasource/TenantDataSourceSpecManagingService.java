package com.olsonsolution.common.spring.domain.service.datasource;

import com.olsonsolution.common.spring.domain.port.repository.datasource.TenantDataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.sterotype.context.TenantContext;

public class TenantDataSourceSpecManagingService extends DataSourceSpecificationManagingService
        implements TenantDataSourceSpecManager {

    @Override
    public void configure(TenantContext tenantContext) {

    }
}
