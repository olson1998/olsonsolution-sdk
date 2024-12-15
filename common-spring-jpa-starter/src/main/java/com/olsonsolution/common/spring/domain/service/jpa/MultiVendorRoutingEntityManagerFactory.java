package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.spring.domain.port.props.jpa.EntityManagerFactoryProperties;
import com.olsonsolution.common.spring.domain.port.props.jpa.JpaProperties;
import com.olsonsolution.common.spring.domain.port.repository.hibernate.RoutingDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingEntityManagerFactory;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.RoutingDataSource;
import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.metamodel.Metamodel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import static org.hibernate.cfg.JdbcSettings.*;
import static org.hibernate.cfg.MappingSettings.DEFAULT_SCHEMA;
import static org.hibernate.cfg.MultiTenancySettings.MULTI_TENANT_CONNECTION_PROVIDER;
import static org.hibernate.cfg.MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER;

@Slf4j
@RequiredArgsConstructor
public class MultiVendorRoutingEntityManagerFactory extends MultiVendorJpaConfigurable<EntityManagerFactory> implements RoutingEntityManagerFactory {

    private static final String PERSISTENCE_UNIT_NAME = "%s.%s.%s.%s_jpa_env";

    private final JpaProperties jpaProperties;

    private final CurrentTenantIdentifierResolver<DataSourceSpec> currentTenantIdentifierResolver;

    private final RoutingDataSourceManager routingDataSourceManager;

    @Override
    public EntityManager createEntityManager() {
        return getDelegate().createEntityManager();
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        return getDelegate().createEntityManager(map);
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        return getDelegate().createEntityManager(synchronizationType);
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
        return getDelegate().createEntityManager(synchronizationType);
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
    public boolean isOpen() {
        return getDelegate().isOpen();
    }

    @Override
    public void close() {
        for(Map.Entry<Class<?>, EntityManagerFactory> sqlDialectEntityManagerFactory : delegatesRegistry.entrySet()) {
            String sqlDialect = sqlDialectEntityManagerFactory.getKey().getSimpleName();
            EntityManagerFactory entityManagerFactory = sqlDialectEntityManagerFactory.getValue();
            try {
                entityManagerFactory.close();
                log.info("Closed entity manager for dialect={}", sqlDialect);
            } catch (Exception e) {
                log.error("Failed to close entity manager factory for dialect={}, reason:", sqlDialect, e);
            }
        }
    }

    @Override
    public Map<String, Object> getProperties() {
        return getDelegate().getProperties();
    }

    @Override
    public Cache getCache() {
        return getDelegate().getCache();
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return getDelegate().getPersistenceUnitUtil();
    }

    @Override
    public void addNamedQuery(String name, Query query) {
        getDelegate().addNamedQuery(name, query);
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return getDelegate().unwrap(cls);
    }

    @Override
    public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
        getDelegate().addNamedEntityGraph(graphName, entityGraph);
    }

    @Override
    protected EntityManagerFactory constructDelegate(DataSourceSpec dataSourceSpec) {
        EntityManagerFactoryProperties entityManagerFactoryProperties =
                getEntityManagerFactoryProperties(jpaEnvironment);
        String unitName = PERSISTENCE_UNIT_NAME.formatted(
                jpaEnvironment.getDialect().getSimpleName(),
                routingDataSource.getId(),
                routingDataSource.getDataBase(),
                routingDataSource.getSchema()
        );
        Properties properties = resolveProperties(entityManagerFactoryProperties, jpaEnvironment);
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setPersistenceUnitName(unitName);
        entityManagerFactoryBean.setJpaProperties(properties);
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        return Objects.requireNonNull(entityManagerFactoryBean.getObject());
    }

    private Properties resolveProperties(EntityManagerFactoryProperties entityManagerFactoryProperties,
                                         DataSourceSpec dataSourceSpec) {
        Properties properties = new Properties(entityManagerFactoryProperties.getProperties());
        properties.setProperty(DIALECT, dataSourceSpec.getDialect().getCanonicalName());
        Optional.ofNullable(dataSourceSpec.getDefaultSchema())
                .ifPresent(schema -> properties.put(DEFAULT_SCHEMA, schema));
        properties.put(SHOW_SQL, entityManagerFactoryProperties.isLogSql());
        properties.put(FORMAT_SQL, entityManagerFactoryProperties.isFormatSqlLog());
        properties.put(MULTI_TENANT_CONNECTION_PROVIDER, routingDataSourceManager);
        properties.put(MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);
        return properties;
    }

    private EntityManagerFactoryProperties getEntityManagerFactoryProperties(DataSourceSpec dataSourceSpec) {
        String schema = dataSourceSpec.getDefaultSchema();
        return jpaProperties.getEntityManagerFactoryProperties()
                .stream()
                .filter(props -> schema.equals(props.getSchema()))
                .findFirst()
                .orElseThrow();
    }

}
