package com.olsonsolution.common.spring.domain.port.repository.jpa;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import jakarta.persistence.EntityManager;

public interface RoutingEntityManager extends EntityManager, DataSourceSpecConfigurable<EntityManager> {

    void registerDelegate(SqlVendor sqlVendor, EntityManager entityManager);

}
