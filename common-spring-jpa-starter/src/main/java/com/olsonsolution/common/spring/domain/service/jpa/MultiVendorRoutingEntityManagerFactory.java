package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.spring.domain.port.props.jpa.EntityManagerFactoryProperties;
import com.olsonsolution.common.spring.domain.port.props.jpa.JpaProperties;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.hibernate.RoutingDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingEntityManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingEntityManagerFactory;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
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
import java.util.Properties;

import static org.hibernate.cfg.JdbcSettings.FORMAT_SQL;
import static org.hibernate.cfg.JdbcSettings.SHOW_SQL;
import static org.hibernate.cfg.MappingSettings.DEFAULT_SCHEMA;
import static org.hibernate.cfg.MultiTenancySettings.MULTI_TENANT_CONNECTION_PROVIDER;
import static org.hibernate.cfg.MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER;

@Slf4j
public class MultiVendorRoutingEntityManagerFactory extends MultiVendorJpaConfigurable<EntityManagerFactory> implements RoutingEntityManagerFactory {

    private static final String PERSISTENCE_UNIT_NAME = "%s_%s_jpa_env";

    private final JpaProperties jpaProperties;

    private final ThreadLocal<SqlVendor> currentSqlVendor;

    private final RoutingEntityManager routingEntityManager;

    private final RoutingDataSourceManager routingDataSourceManager;

    private final CurrentTenantIdentifierResolver<DataSourceSpec> datasSourceSpecResolver;

    public MultiVendorRoutingEntityManagerFactory(JpaProperties jpaProperties,
                                                  DestinationDataSourceProvider destinationDataSourceProvider,
                                                  RoutingEntityManager routingEntityManager,
                                                  RoutingDataSourceManager routingDataSourceManager,
                                                  CurrentTenantIdentifierResolver<DataSourceSpec> datasSourceSpecResolver) {
        super(destinationDataSourceProvider);
        this.jpaProperties = jpaProperties;
        this.currentSqlVendor = new ThreadLocal<>();
        this.routingEntityManager = routingEntityManager;
        this.routingDataSourceManager = routingDataSourceManager;
        this.datasSourceSpecResolver = datasSourceSpecResolver;
    }

    @Override
    public EntityManager createEntityManager() {
        SqlVendor sqlVendor = getCurrentSqlVendor();
        EntityManager entityManager = getDelegate().createEntityManager();
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
        for (Map.Entry<SqlVendor, EntityManagerFactory> sqlVendorEntityManagerFactory : delegatesRegistry.entrySet()) {
            SqlVendor sqlVendor = sqlVendorEntityManagerFactory.getKey();
            EntityManagerFactory entityManagerFactory = sqlVendorEntityManagerFactory.getValue();
            try {
                entityManagerFactory.close();
                log.info("Closed entity manager factory. Vendor: {} factory: {}", sqlVendor, entityManagerFactory);
            } catch (Exception e) {
                log.error("Failed to close entity manager factory for vendor: {}, reason:", sqlVendor, e);
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
    protected void setSqlVendorSpec(SqlVendor sqlVendor, DataSourceSpec dataSourceSpec) {
        super.setSqlVendorSpec(sqlVendor, dataSourceSpec);
        currentSqlVendor.set(sqlVendor);
    }

    @Override
    public void clear() {
        currentSqlVendor.remove();
        super.clear();
    }

    @Override
    protected EntityManagerFactory constructDelegate(SqlVendor sqlVendor, DataSourceSpec dataSourceSpec) {
        EntityManagerFactoryProperties entityManagerFactoryProperties =
                getEntityManagerFactoryProperties(dataSourceSpec);
        String unitName = PERSISTENCE_UNIT_NAME.formatted(
                dataSourceSpec.getName(),
                dataSourceSpec.getDefaultSchema()
        );
        Properties properties = resolveProperties(entityManagerFactoryProperties);
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setPersistenceUnitName(unitName);
        entityManagerFactoryBean.setJpaProperties(properties);
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        return Objects.requireNonNull(entityManagerFactoryBean.getObject());
    }

    private Properties resolveProperties(EntityManagerFactoryProperties entityManagerFactoryProperties) {
        Properties properties = new Properties(entityManagerFactoryProperties.getProperties());
        properties.put(DEFAULT_SCHEMA, entityManagerFactoryProperties.getSchema());
        properties.put(SHOW_SQL, entityManagerFactoryProperties.isLogSql());
        properties.put(FORMAT_SQL, entityManagerFactoryProperties.isFormatSqlLog());
        properties.put(MULTI_TENANT_CONNECTION_PROVIDER, routingDataSourceManager);
        properties.put(MULTI_TENANT_IDENTIFIER_RESOLVER, datasSourceSpecResolver);
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

    private SqlVendor getCurrentSqlVendor() {
        SqlVendor sqlVendor = currentSqlVendor.get();
        if (sqlVendor == null) {
            throw new IllegalStateException("No current sql vendor was found");
        }
        return sqlVendor;
    }

}
