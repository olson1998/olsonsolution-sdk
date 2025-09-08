package com.olsonsolution.common.spring.domain.port.sterotype.context;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.spring.domain.port.stereotype.context.LocalContext;
import com.olsonsolution.common.spring.domain.port.sterotype.tenant.Tenant;

public interface TenantContext extends LocalContext {

    Tenant getTenant();

    SqlPermission getPermission();

}
