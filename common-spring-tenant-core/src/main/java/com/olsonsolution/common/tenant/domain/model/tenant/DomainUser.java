package com.olsonsolution.common.tenant.domain.model.tenant;

import com.olsonsolution.common.tenant.domain.port.sterotype.tenant.AuthInfo;
import com.olsonsolution.common.tenant.domain.port.sterotype.tenant.User;
import com.olsonsolution.common.tenant.domain.port.sterotype.tenant.UserInfo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DomainUser implements User {

    private final String id;

    private final String username;

    private final UserInfo info;

    private final AuthInfo authInfo;

}
