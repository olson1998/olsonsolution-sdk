package com.olsonsolution.common.spring.domain.service.datasource;

import com.olsonsolution.common.spring.domain.model.datasource.DataSourceSpecification;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.tenant.TenantContextAware;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import com.olsonsolution.common.spring.domain.port.sterotype.context.TenantContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TenantDataSourceSpecConfigurationService implements TenantContextAware {

    private final DataSourceSpecManager dataSourceSpecManager;

    @Override
    public void configure(TenantContext tenantContext) {
        DataSourceSpec dataSourceSpec = DataSourceSpecification.builder()
                .name(tenantContext.getTenant().getId())
                .build();
        dataSourceSpecManager.setThreadLocal(dataSourceSpec);
    }
}
