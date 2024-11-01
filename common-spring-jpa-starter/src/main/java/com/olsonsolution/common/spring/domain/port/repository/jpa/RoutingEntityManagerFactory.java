package com.olsonsolution.common.spring.domain.port.repository.jpa;

import jakarta.persistence.EntityManagerFactory;

public interface RoutingEntityManagerFactory extends EntityManagerFactory, JpaEnvironmentConfigurable<EntityManagerFactory> {

}
