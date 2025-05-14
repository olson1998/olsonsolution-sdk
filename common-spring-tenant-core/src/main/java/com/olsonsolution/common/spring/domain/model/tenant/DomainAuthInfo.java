package com.olsonsolution.common.spring.domain.model.tenant;

import com.olsonsolution.common.spring.domain.port.sterotype.authentication.AuthScheme;
import com.olsonsolution.common.spring.domain.port.sterotype.tenant.AuthInfo;
import lombok.Data;

import java.util.Set;

@Data
public class DomainAuthInfo<T> implements AuthInfo<T> {

    private final AuthScheme scheme;

    private final Set<String> roles;

    private final Set<String> scopes;

    private final T credential;

}
