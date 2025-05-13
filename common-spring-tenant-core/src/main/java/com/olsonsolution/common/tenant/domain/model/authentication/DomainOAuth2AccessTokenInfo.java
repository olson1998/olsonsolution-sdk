package com.olsonsolution.common.tenant.domain.model.authentication;

import com.nimbusds.jwt.JWT;
import com.olsonsolution.common.tenant.domain.model.tenant.DomainAuthInfo;
import com.olsonsolution.common.tenant.domain.port.sterotype.authentication.AuthScheme;
import com.olsonsolution.common.tenant.domain.port.sterotype.authentication.OAuth2AccessTokenInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DomainOAuth2AccessTokenInfo extends DomainAuthInfo<JWT> implements OAuth2AccessTokenInfo {

    private final String clientId;

    public DomainOAuth2AccessTokenInfo(AuthScheme scheme, Set<String> roles, Set<String> scopes, JWT credential,
                                       String clientId) {
        super(scheme, roles, scopes, credential);
        this.clientId = clientId;
    }
}
