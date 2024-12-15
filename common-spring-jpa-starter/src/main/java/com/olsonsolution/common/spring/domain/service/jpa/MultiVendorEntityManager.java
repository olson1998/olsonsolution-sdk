package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingEntityManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingEntityManagerFactory;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.metamodel.Metamodel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class MultiVendorEntityManager extends MultiVendorJpaConfigurable<EntityManager> implements RoutingEntityManager {

    private final RoutingEntityManagerFactory routingEntityManagerFactory;

    @Override
    protected EntityManager constructDelegate(DataSourceSpec dataSourceSpec) {
        return routingEntityManagerFactory.createEntityManager();
    }

    @Override
    public void persist(Object entity) {
        getDelegate().persist(entity);
    }

    @Override
    public <T> T merge(T entity) {
        return getDelegate().merge(entity);
    }

    @Override
    public void remove(Object entity) {
        getDelegate().remove(entity);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return getDelegate().find(entityClass, primaryKey);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return getDelegate().find(entityClass, primaryKey, properties);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return getDelegate().find(entityClass, primaryKey, lockMode);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        return getDelegate().find(entityClass, primaryKey, lockMode, properties);
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return getDelegate().getReference(entityClass, primaryKey);
    }

    @Override
    public void flush() {
        getDelegate().flush();
    }

    @Override
    public void setFlushMode(FlushModeType flushMode) {
        getDelegate().setFlushMode(flushMode);
    }

    @Override
    public FlushModeType getFlushMode() {
        return getDelegate().getFlushMode();
    }

    @Override
    public void lock(Object entity, LockModeType lockMode) {
        getDelegate().lock(entity, lockMode);
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        getDelegate().lock(entity, lockMode, properties);
    }

    @Override
    public void refresh(Object entity) {
        getDelegate().refresh(entity);
    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
        getDelegate().refresh(entity, properties);
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode) {
        getDelegate().refresh(entity, lockMode);
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        getDelegate().refresh(entity, lockMode, properties);
    }

    @Override
    public void detach(Object entity) {
        getDelegate().detach(entity);
    }

    @Override
    public boolean contains(Object entity) {
        return getDelegate().contains(entity);
    }

    @Override
    public LockModeType getLockMode(Object entity) {
        return getDelegate().getLockMode(entity);
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        getDelegate().setProperty(propertyName, value);
    }

    @Override
    public Map<String, Object> getProperties() {
        return getDelegate().getProperties();
    }

    @Override
    public Query createQuery(String qlString) {
        return getDelegate().createQuery(qlString);
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return getDelegate().createQuery(criteriaQuery);
    }

    @Override
    public Query createQuery(CriteriaUpdate updateQuery) {
        return getDelegate().createQuery(updateQuery);
    }

    @Override
    public Query createQuery(CriteriaDelete deleteQuery) {
        return getDelegate().createQuery(deleteQuery);
    }

    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return getDelegate().createQuery(qlString, resultClass);
    }

    @Override
    public Query createNamedQuery(String name) {
        return getDelegate().createNamedQuery(name);
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return getDelegate().createNamedQuery(name, resultClass);
    }

    @Override
    public Query createNativeQuery(String sqlString) {
        return getDelegate().createNativeQuery(sqlString);
    }

    @Override
    public Query createNativeQuery(String sqlString, Class resultClass) {
        return getDelegate().createNativeQuery(sqlString, resultClass);
    }

    @Override
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return getDelegate().createNativeQuery(sqlString, resultSetMapping);
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        return getDelegate().createNamedStoredProcedureQuery(name);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        return getDelegate().createStoredProcedureQuery(procedureName);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        return getDelegate().createStoredProcedureQuery(procedureName, resultClasses);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        return getDelegate().createStoredProcedureQuery(procedureName, resultSetMappings);
    }

    @Override
    public void joinTransaction() {
        getDelegate().joinTransaction();
    }

    @Override
    public boolean isJoinedToTransaction() {
        return getDelegate().isJoinedToTransaction();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return getDelegate().unwrap(cls);
    }

    @Override
    public void close() {
        for (Map.Entry<Class<?>, EntityManager> sqlDialectEntityManager : delegatesRegistry.entrySet()) {
            String sqlDialect = sqlDialectEntityManager.getKey().getSimpleName();
            EntityManager entityManager = sqlDialectEntityManager.getValue();
            try {
                entityManager.close();
            } catch (Exception e) {
                log.error("Failed to close entity manager for dialect={}, reason:", sqlDialect, e);
            }
        }
    }

    @Override
    public boolean isOpen() {
        return getDelegate().isOpen();
    }

    @Override
    public EntityTransaction getTransaction() {
        return getDelegate().getTransaction();
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return getDelegate().getEntityManagerFactory();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return getDelegate().getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return getDelegate().getMetamodel();
    }

    @Override
    public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
        return getDelegate().createEntityGraph(rootType);
    }

    @Override
    public EntityGraph<?> createEntityGraph(String graphName) {
        return getDelegate().createEntityGraph(graphName);
    }

    @Override
    public EntityGraph<?> getEntityGraph(String graphName) {
        return getDelegate().getEntityGraph(graphName);
    }

    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
        return getDelegate().getEntityGraphs(entityClass);
    }
}
