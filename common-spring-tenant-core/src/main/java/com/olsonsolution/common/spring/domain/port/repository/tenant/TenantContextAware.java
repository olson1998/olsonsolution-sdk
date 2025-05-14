package com.olsonsolution.common.spring.domain.port.repository.tenant;

import com.olsonsolution.common.spring.domain.port.sterotype.context.TenantContext;

public interface TenantContextAware {

    void configure(TenantContext tenantContext);

}
