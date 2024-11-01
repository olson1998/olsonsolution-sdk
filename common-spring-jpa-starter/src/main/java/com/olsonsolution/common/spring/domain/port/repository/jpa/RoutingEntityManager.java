package com.olsonsolution.common.spring.domain.port.repository.jpa;

import jakarta.persistence.EntityManager;

public interface RoutingEntityManager extends EntityManager, JpaEnvironmentConfigurable<EntityManager> {
}
