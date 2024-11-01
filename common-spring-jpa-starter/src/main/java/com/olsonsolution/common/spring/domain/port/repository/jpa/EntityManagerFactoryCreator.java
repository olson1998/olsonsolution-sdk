package com.olsonsolution.common.spring.domain.port.repository.jpa;

import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;
import jakarta.persistence.EntityManagerFactory;

public interface EntityManagerFactoryCreator {

    EntityManagerFactory fabricate(JpaEnvironment jpaEnvironment);

}
