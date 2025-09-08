package com.olsonsolution.common.spring.domain.port.sterotype.tenant;

public interface User {

    String getId();

    String getUsername();

    UserInfo getInfo();

    AuthInfo<?> getAuthInfo();

}
