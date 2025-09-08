package com.olsonsolution.common.spring.domain.model.tenant;

import com.olsonsolution.common.spring.domain.port.sterotype.tenant.User;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class DomainUser implements User {

    private final String id;

    private final String username;

    private final Set<String> roles;

}
