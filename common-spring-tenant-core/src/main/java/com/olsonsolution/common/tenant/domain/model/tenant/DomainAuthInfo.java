package com.olsonsolution.common.tenant.domain.model.tenant;

import com.olsonsolution.common.tenant.domain.port.sterotype.authentication.AuthScheme;
import com.olsonsolution.common.tenant.domain.port.sterotype.tenant.AuthInfo;
import lombok.Data;

import java.util.Set;

@Data
public class DomainAuthInfo<T> implements AuthInfo<T> {

    private final AuthScheme scheme;

    private final Set<String> roles;

    private final Set<String> scopes;

    private final T credential;

}
