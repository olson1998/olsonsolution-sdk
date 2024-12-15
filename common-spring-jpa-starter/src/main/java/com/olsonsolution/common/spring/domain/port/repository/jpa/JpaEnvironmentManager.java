package com.olsonsolution.common.spring.domain.port.repository.jpa;

import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;

public interface JpaEnvironmentManager {

    JpaEnvironment getThreadLocal();

    void setCurrent(JpaEnvironment jpaEnvironment);

    void clear();

}
