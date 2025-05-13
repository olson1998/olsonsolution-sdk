package com.olsonsolution.common.tenant.domain.port.sterotype.context;

import com.olsonsolution.common.tenant.domain.port.sterotype.tenant.User;

public interface TenantUserContext extends TenantContext {

    User getUser();

}
