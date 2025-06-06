package com.olsonsolution.common.spring.domain.port.repository.datasource;

import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaSpecDataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.tenant.TenantContextAware;

public interface TenantDataSourceSpecManager extends JpaSpecDataSourceSpecManager, TenantContextAware {
}
