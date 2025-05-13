package com.olsonsolution.common.tenant.domain.port.sterotype.tenant;

public interface User {

    String getId();

    String getUsername();

    UserInfo getInfo();

    AuthInfo<?> getAuthInfo();

}
