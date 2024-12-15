package com.olsonsolution.common.spring.domain.port.repository.jpa;

import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;

public interface JpaEnvironmentConfigurer {

    void configure(JpaEnvironment jpaEnvironment);

    void clear();

}
