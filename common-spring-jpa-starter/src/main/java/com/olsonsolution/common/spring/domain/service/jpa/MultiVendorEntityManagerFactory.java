package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.data.domain.model.sql.SqlVendors;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.spring.domain.port.props.jpa.JpaSpecProperties;
import com.olsonsolution.common.spring.domain.port.props.jpa.JpaProperties;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.EntityManagerFactoryDelegate;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.metamodel.Metamodel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.dialect.*;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.util.*;

import static java.util.Map.entry;
import static org.hibernate.cfg.JdbcSettings.*;
import static org.hibernate.cfg.MappingSettings.DEFAULT_SCHEMA;
import static org.hibernate.cfg.MultiTenancySettings.MULTI_TENANT_CONNECTION_PROVIDER;
import static org.hibernate.cfg.MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER;

@Slf4j
public class MultiVendorEntityManagerFactory extends MultiVendorJpaConfigurable<EntityManagerFactory> implements EntityManagerFactoryDelegate {

    private static final String PERSISTENCE_UNIT_NAME = "%s_%s_jpa";

    private static final Map<SqlVendor, Class<? extends Dialect>> SQL_VENDOR_DIALECT = Map.ofEntries(
            entry(SqlVendors.SQL_SERVER, SQLServerDialect.class),
            entry(SqlVendors.POSTGRESQL, PostgreSQLDialect.class),
            entry(SqlVendors.DB2, DB2Dialect.class),
            entry(SqlVendors.MARIADB, MariaDBDialect.class)
    );

    private final String schema;

    private final String jpaSpecName;

    private final JpaProperties jpaProperties;

    private final DestinationDataSourceManager destinationDataSourceManager;

    public MultiVendorEntityManagerFactory(String schema,
                                           String jpaSpecName,
                                           JpaProperties jpaProperties,
                                           DataSourceSpecManager dataSourceSpecManager,
                                           SqlDataSourceProvider sqlDataSourceProvider,
                                           DestinationDataSourceManager routingDataSourceManager,
                                           CurrentTenantIdentifierResolver<DataSourceSpec> dataSourceSpecResolver) {
        super(dataSourceSpecManager, sqlDataSourceProvider);
        this.schema = schema;
        this.jpaSpecName = jpaSpecName;
        this.jpaProperties = jpaProperties;
        this.destinationDataSourceManager = routingDataSourceManager;
    }

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
        return getDelegate().createEntityManager(synchronizationType, map);
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
    protected EntityManagerFactory constructDelegate(SqlVendor sqlVendor) {
        String unitName = PERSISTENCE_UNIT_NAME.formatted(schema, sqlVendor.name());
        Optional<? extends JpaSpecProperties> properties = findEntityManagerFactoryProperties();
        Properties jpaProperties = properties
                .map(p -> resolveProperties(p, sqlVendor))
                .orElseGet(Properties::new);
        String[] basePackages = properties.map(JpaSpecProperties::getEntityProperties)
                .map(props -> props.getPackagesToScan()
                        .toArray(String[]::new))
                .orElseGet(() -> new String[0]);
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setPersistenceUnitName(unitName);
        entityManagerFactoryBean.setJpaProperties(jpaProperties);
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManagerFactoryBean.setPackagesToScan(basePackages);
        entityManagerFactoryBean.afterPropertiesSet();
        EntityManagerFactory entityManagerFactory = Objects.requireNonNull(entityManagerFactoryBean.getObject());
        log.info(
                "Created entity manager factory, Jpa Spec: '{}' schema: '{}' SQL vendor: '{}'",
                jpaSpecName, schema, sqlVendor
        );
        return entityManagerFactory;
    }

    private Properties resolveProperties(JpaSpecProperties jpaSpecProperties,
                                         SqlVendor vendor) {
        Properties properties = new Properties(jpaSpecProperties.getProperties());
        properties.put(DEFAULT_SCHEMA, jpaSpecProperties.getSchema());
        properties.put(SHOW_SQL, jpaSpecProperties.isLogSql());
        properties.put(FORMAT_SQL, jpaSpecProperties.isFormatSqlLog());
        properties.put(MULTI_TENANT_CONNECTION_PROVIDER, destinationDataSourceManager);
        properties.put(MULTI_TENANT_IDENTIFIER_RESOLVER, dataSourceSpecManager);
        Optional.ofNullable(SQL_VENDOR_DIALECT.get(vendor))
                .ifPresent(dialect -> properties.put(DIALECT, dialect));
        return properties;
    }

    private Optional<? extends JpaSpecProperties> findEntityManagerFactoryProperties() {
        return jpaProperties.getJpaSpecificationsProperties()
                .stream()
                .filter(props -> StringUtils.equals(jpaSpecName, props.getName()))
                .findFirst();
    }

}
