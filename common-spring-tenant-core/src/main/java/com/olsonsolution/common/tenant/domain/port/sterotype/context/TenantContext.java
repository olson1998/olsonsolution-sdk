package com.olsonsolution.common.tenant.domain.port.sterotype.context;

import com.olsonsolution.common.spring.domain.port.stereotype.context.LocalContext;
import com.olsonsolution.common.tenant.domain.port.sterotype.tenant.Tenant;

public interface TenantContext extends LocalContext {

    Tenant getTenant();

}
