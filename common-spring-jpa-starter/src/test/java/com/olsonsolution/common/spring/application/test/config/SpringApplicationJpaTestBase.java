package com.olsonsolution.common.spring.application.test.config;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.olsonsolution.common.migration.domain.port.repository.MigrationService;
import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResults;
import com.olsonsolution.common.spring.application.async.config.AsyncConfig;
import com.olsonsolution.common.spring.application.async.props.AsyncProperties;
import com.olsonsolution.common.spring.application.caching.InMemoryCachingConfig;
import com.olsonsolution.common.spring.application.config.time.TimeUtilsConfig;
import com.olsonsolution.common.spring.application.jpa.config.*;
import com.olsonsolution.common.spring.application.jpa.props.SpringApplicationDestinationDataSourceProperties;
import com.olsonsolution.common.spring.application.jpa.props.SpringApplicationJpaProperties;
import com.olsonsolution.common.spring.application.migration.config.ChangeLogProviderConfig;
import com.olsonsolution.common.spring.application.migration.config.LiquibaseConfig;
import com.olsonsolution.common.spring.application.migration.config.SqlVendorSupportersConfig;
import com.olsonsolution.common.spring.application.migration.props.LiquibaseProperties;
import com.olsonsolution.common.spring.application.props.time.JodaDateTimeProperties;
import com.olsonsolution.common.spring.domain.model.datasource.DomainDataSourceSpecification;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaSpecDataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.olsonsolution.common.data.domain.model.sql.SqlPermissions.RWX;
import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.*;
import static com.olsonsolution.common.spring.application.jpa.config.DataSourceModelersConfig.SPRING_APPLICATION_JPA_DATA_SOURCE_MODELERS_PROPERTIES_PREFIX;
import static com.olsonsolution.common.spring.application.jpa.props.SpringApplicationDestinationDataSourceProperties.SPRING_APPLICATION_JPA_DESTINATION_DATA_SOURCE_PROPERTIES_PREFIX;
import static com.olsonsolution.common.spring.application.jpa.props.SpringApplicationJpaProperties.SPRING_APPLICATION_JPA_PROPERTIES_PREFIX;
import static com.olsonsolution.common.spring.application.props.time.JodaDateTimeProperties.JODA_DATE_TIME_PROPERTIES_PREFIX;
import static com.olsonsolution.common.spring.application.test.config.SpringApplicationJpaTestBase.CLASSIC_ENTITY_PACKAGE;
import static com.olsonsolution.common.spring.application.test.config.SpringApplicationJpaTestBase.CLASSIC_REPO_PACKAGE;
import static org.assertj.core.api.Assertions.assertThat;

@EnableJpaAuditing
@EnableTransactionManagement
@EnableConfigurationProperties
@ContextConfiguration(classes = {
        TimeUtilsConfig.class,
        ChangeLogProviderConfig.class,
        DataSourceSpecConfig.class,
        DataSourceModelersConfig.class,
        SqlVendorSupportersConfig.class,
        AsyncConfig.class,
        JpaConfig.class,
        LiquibaseConfig.class,
        InMemoryCachingConfig.class,
        AuditableEntityListenerConfig.class,
        SqlVendorPropertiesResolverConfig.class,
        AsyncProperties.class,
        JodaDateTimeProperties.class,
        LiquibaseProperties.class,
        SpringApplicationJpaProperties.class,
        SpringApplicationDestinationDataSourceProperties.class
})
@ComponentScan(basePackages = "com.olsonsolution.common.spring.application.config.jpa.test")
@ExtendWith(SpringExtension.class)
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
        JODA_DATE_TIME_PROPERTIES_PREFIX + ".time-zone=America/New_York",
        SPRING_APPLICATION_JPA_DATA_SOURCE_MODELERS_PROPERTIES_PREFIX + ".sql-server=enabled",
        SPRING_APPLICATION_JPA_DATA_SOURCE_MODELERS_PROPERTIES_PREFIX + ".postgresql=enabled",
        SPRING_APPLICATION_JPA_DATA_SOURCE_MODELERS_PROPERTIES_PREFIX + ".mariadb=enabled",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.0.name=WarehouseIndex",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.0.schema=warehouse_index",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.0.log-sql=true",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.0.format-sql-log=true",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.0.entity.packages-to-scan.0=" + CLASSIC_ENTITY_PACKAGE,
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.0.repository.packages-to-scan.0=" + CLASSIC_REPO_PACKAGE,
        "spring.jpa.show-sql=true"
})
public abstract class SpringApplicationJpaTestBase implements InitializingBean {

    protected static final String DATABASE = "unit_test";

    protected static final String PASSWORD = "My$uperSeccretPass@@@";

    protected static final String SQL_SERVER_DATASOURCE = "SQLSERVER";

    protected static final String POSTGRES_DATASOURCE = "POSTGRES";

    protected static final String MARIADB_DATASOURCE = "MARIADB";

    protected static final String CLASSIC_ENTITY_PACKAGE =
            "com.olsonsolution.common.spring.application.datasource.classic.entity";

    protected static final String CLASSIC_REPO_PACKAGE =
            "com.olsonsolution.common.spring.application.datasource.classic.repository";

    @Container
    private static final MSSQLServerContainer<?> SQL_SERVER_CONTAINER = new MSSQLServerContainer<>()
            .withPassword(PASSWORD)
            .acceptLicense();

    @Container
    private static final PostgreSQLContainer POSTGRES_CONTAINER =
            new PostgreSQLContainer("postgres:14-alpine")
                    .withUsername("sa")
                    .withPassword(PASSWORD);

    @Container
    private static final MariaDBContainer<?> MARIA_DB_CONTAINER =
            new MariaDBContainer<>("mariadb:10.6.1")
                    .withEnv("MARIADB_ROOT_PASSWORD", PASSWORD);

    @Autowired
    private MigrationService migrationService;

    @Autowired
    private JpaSpecDataSourceSpecManager jpaSpecDataSourceSpecManager;

    @Autowired
    private DestinationDataSourceManager destinationDataSourceManager;

    public static Stream<DataSourceSpecification> dataSourceSpecStream() {
        return Stream.of(
                new DomainDataSourceSpecification(SQL_SERVER_DATASOURCE, RWX),
                new DomainDataSourceSpecification(POSTGRES_DATASOURCE, RWX),
                new DomainDataSourceSpecification(MARIADB_DATASOURCE, RWX)
        );
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (DataSourceSpecification dataSourceSpecification : dataSourceSpecStream().toList()) {
            jpaSpecDataSourceSpecManager.setThreadLocal(dataSourceSpecification);
            DataSource dataSource = destinationDataSourceManager.selectDataSourceBySpec(dataSourceSpecification);
            MigrationResults migrationResults = migrationService.migrateAsync(dataSource)
                    .get(30, TimeUnit.SECONDS);
            jpaSpecDataSourceSpecManager.clear();
        }
    }

    @BeforeAll
    static void setupDataBase() throws SQLException {
        createSQLServerTestEnv();
        createPostgresEnv();
    }

    @DynamicPropertySource
    static void setDataSourceProperties(DynamicPropertyRegistry registry) {
        String prefix = SPRING_APPLICATION_JPA_DESTINATION_DATA_SOURCE_PROPERTIES_PREFIX + ".instance";
        registry.add(prefix + ".0.name", () -> SQL_SERVER_DATASOURCE);
        registry.add(prefix + ".0.data-source.vendor", SQL_SERVER::name);
        registry.add(prefix + ".0.data-source.host", SQL_SERVER_CONTAINER::getHost);
        registry.add(prefix + ".0.data-source.port", () -> SQL_SERVER_CONTAINER.getMappedPort(1433));
        registry.add(prefix + ".0.data-source.database", () -> DATABASE);
        registry.add(prefix + ".0.data-source.user.rwx.0.username", SQL_SERVER_CONTAINER::getUsername);
        registry.add(prefix + ".0.data-source.user.rwx.0.password", SQL_SERVER_CONTAINER::getPassword);
        registry.add(prefix + ".0.data-source.property.0.name", () -> "trustServerCertificate");
        registry.add(prefix + ".0.data-source.property.0.value", () -> "true");
        registry.add(prefix + ".0.data-source.property.1.name", () -> "encrypt");
        registry.add(prefix + ".0.data-source.property.1.value", () -> "false");
        registry.add(prefix + ".1.name", () -> POSTGRES_DATASOURCE);
        registry.add(prefix + ".1.data-source.vendor", POSTGRESQL::name);
        registry.add(prefix + ".1.data-source.host", POSTGRES_CONTAINER::getHost);
        registry.add(prefix + ".1.data-source.port", () -> POSTGRES_CONTAINER.getMappedPort(5432));
        registry.add(prefix + ".1.data-source.database", () -> DATABASE);
        registry.add(prefix + ".1.data-source.user.rwx.0.username", POSTGRES_CONTAINER::getUsername);
        registry.add(prefix + ".1.data-source.user.rwx.0.password", POSTGRES_CONTAINER::getPassword);
        registry.add(prefix + ".2.name", () -> MARIADB_DATASOURCE);
        registry.add(prefix + ".2.data-source.vendor", MARIADB::name);
        registry.add(prefix + ".2.data-source.host", MARIA_DB_CONTAINER::getHost);
        registry.add(prefix + ".2.data-source.port", () -> MARIA_DB_CONTAINER.getMappedPort(3306));
        registry.add(prefix + ".2.data-source.database", MARIA_DB_CONTAINER::getDatabaseName);
        registry.add(prefix + ".2.data-source.user.rwx.0.username", () -> "root");
        registry.add(prefix + ".2.data-source.user.rwx.0.password", () -> PASSWORD);
    }

    private static void createSQLServerTestEnv() throws SQLException {
        String host = SQL_SERVER_CONTAINER.getHost();
        Integer port = SQL_SERVER_CONTAINER.getMappedPort(1433);
        String username = SQL_SERVER_CONTAINER.getUsername();
        String password = SQL_SERVER_CONTAINER.getPassword();
        SQLServerDataSource sqlServerDataSource = new SQLServerDataSource();
        sqlServerDataSource.setServerName(host);
        sqlServerDataSource.setPortNumber(port);
        sqlServerDataSource.setUser(username);
        sqlServerDataSource.setPassword(password);
        sqlServerDataSource.setEncrypt("false");
        sqlServerDataSource.setTrustServerCertificate(true);
        try (Connection connection = sqlServerDataSource.getConnection();
             PreparedStatement query = connection.prepareStatement("CREATE DATABASE " + DATABASE)) {
            query.execute();
        }
    }

    private static void createPostgresEnv() throws SQLException {
        String host = POSTGRES_CONTAINER.getHost();
        Integer port = POSTGRES_CONTAINER.getMappedPort(5432);
        String username = POSTGRES_CONTAINER.getUsername();
        String password = POSTGRES_CONTAINER.getPassword();
        PGSimpleDataSource postgresDataSource = new PGSimpleDataSource();
        postgresDataSource.setServerNames(new String[]{host});
        postgresDataSource.setPortNumbers(new int[]{port});
        postgresDataSource.setUser(username);
        postgresDataSource.setPassword(password);
        postgresDataSource.setDatabaseName("postgres");
        try (Connection connection = postgresDataSource.getConnection();
             PreparedStatement query = connection.prepareStatement("CREATE DATABASE " + DATABASE)) {
            query.execute();
        }
    }


}
