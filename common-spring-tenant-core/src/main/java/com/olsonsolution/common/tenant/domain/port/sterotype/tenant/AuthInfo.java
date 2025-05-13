package com.olsonsolution.common.tenant.domain.port.sterotype.tenant;

import com.olsonsolution.common.tenant.domain.port.sterotype.authentication.AuthScheme;

import java.util.Set;

public interface AuthInfo<C> {

    AuthScheme getScheme();

    Set<String> getRoles();

    Set<String> getScopes();

    C getCredential();

}
