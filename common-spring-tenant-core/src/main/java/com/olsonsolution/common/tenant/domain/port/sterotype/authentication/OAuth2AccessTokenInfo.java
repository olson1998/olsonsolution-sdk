package com.olsonsolution.common.tenant.domain.port.sterotype.authentication;

import com.nimbusds.jwt.JWT;
import com.olsonsolution.common.tenant.domain.port.sterotype.tenant.AuthInfo;

public interface OAuth2AccessTokenInfo extends AuthInfo<JWT> {

    String getClientId();

}
