package com.olsonsolution.common.spring.domain.port.repository.jpa;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import jakarta.persistence.EntityManager;

import java.util.function.Supplier;

public interface RoutingEntityManager extends EntityManager, DataSourceSpecConfigurable<EntityManager> {

    void registerDelegate(SqlVendor sqlVendor, EntityManager entityManager);

    void setDelegateFactory(Supplier<EntityManager> entityManagerFactory);

}
