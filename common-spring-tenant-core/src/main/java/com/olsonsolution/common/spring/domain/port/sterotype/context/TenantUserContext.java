package com.olsonsolution.common.spring.domain.port.sterotype.context;

import com.olsonsolution.common.spring.domain.port.sterotype.tenant.User;

public interface TenantUserContext extends TenantContext {

    User getUser();

}
