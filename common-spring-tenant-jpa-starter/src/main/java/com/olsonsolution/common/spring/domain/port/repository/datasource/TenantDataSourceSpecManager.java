package com.olsonsolution.common.spring.domain.port.repository.datasource;

import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.tenant.TenantContextAware;

public interface TenantDataSourceSpecManager extends DataSourceSpecManager, TenantContextAware {
}
