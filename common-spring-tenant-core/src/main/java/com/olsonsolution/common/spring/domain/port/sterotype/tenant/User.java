package com.olsonsolution.common.spring.domain.port.sterotype.tenant;

import java.util.Set;

public interface User {

    String getId();

    String getUsername();

    Set<String> getRoles();

}
