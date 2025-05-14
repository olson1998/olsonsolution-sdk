package com.olsonsolution.common.spring.domain.port.sterotype.tenant;

import com.olsonsolution.common.spring.domain.port.sterotype.authentication.AuthScheme;

import java.util.Set;

public interface AuthInfo<C> {

    AuthScheme getScheme();

    Set<String> getRoles();

    Set<String> getScopes();

    C getCredential();

}
