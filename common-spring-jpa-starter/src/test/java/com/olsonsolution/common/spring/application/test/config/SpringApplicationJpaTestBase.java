package com.olsonsolution.common.spring.application.test.config;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.olsonsolution.common.data.domain.port.repository.sql.SqlDataSourceFactory;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.migration.domain.port.repository.MigrationService;
import com.olsonsolution.common.spring.application.async.config.AsyncConfig;
import com.olsonsolution.common.spring.application.async.props.AsyncProperties;
import com.olsonsolution.common.spring.application.caching.InMemoryCachingConfig;
import com.olsonsolution.common.spring.application.config.time.TimeUtilsConfig;
import com.olsonsolution.common.spring.application.datasource.config.DestinationDataSourcePropertyLookupServiceConfig;
import com.olsonsolution.common.spring.application.datasource.props.ApplicationDestinationDataSourceProperties;
import com.olsonsolution.common.spring.application.datasource.props.ApplicationSqlVendorSupportProperties;
import com.olsonsolution.common.spring.application.jpa.config.*;
import com.olsonsolution.common.spring.application.jpa.props.SpringApplicationJpaProperties;
import com.olsonsolution.common.spring.application.migration.config.ChangeLogProviderConfig;
import com.olsonsolution.common.spring.application.migration.config.LiquibaseConfig;
import com.olsonsolution.common.spring.application.migration.config.SqlVendorSupportersConfig;
import com.olsonsolution.common.spring.application.migration.props.LiquibaseProperties;
import com.olsonsolution.common.spring.application.props.time.JodaDateTimeProperties;
import com.olsonsolution.common.spring.domain.model.datasource.DomainDataSourceSpec;
import com.olsonsolution.common.spring.domain.model.datasource.DomainJpaSpecDataSource;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaSpecDataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.JpaDataSourceSpec;
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
import java.util.stream.Stream;

import static com.olsonsolution.common.data.domain.model.sql.SqlPermissions.RWX;
import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.*;
import static com.olsonsolution.common.spring.domain.model.datasource.DomainJpaSpecDataSource.SYSTEM_JPA_SPEC;

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
        DestinationDataSourcePropertyLookupServiceConfig.class,
        AsyncProperties.class,
        JodaDateTimeProperties.class,
        LiquibaseProperties.class,
        SpringApplicationJpaProperties.class,
        ApplicationSqlVendorSupportProperties.class,
        ApplicationDestinationDataSourceProperties.class
})
@ComponentScan(basePackages = "com.olsonsolution.common.spring.application.config.jpa.test")
@ExtendWith(SpringExtension.class)
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(locations = "classpath:./application-spring-common.properties")
public abstract class SpringApplicationJpaTestBase implements InitializingBean {

    protected static final String DATABASE = "unit_test";

    protected static final String PASSWORD = "My$uperSeccretPass@@@";

    protected static final String SQL_SERVER_DATASOURCE = "SQLSERVER";

    protected static final String POSTGRES_DATASOURCE = "POSTGRES";

    protected static final String MARIADB_DATASOURCE = "MARIADB";

    @Container
    private static final MSSQLServerContainer<?> SQL_SERVER_CONTAINER = new MSSQLServerContainer<>()
            .withPassword(PASSWORD)
            .acceptLicense();

    @Container
    private static final PostgreSQLContainer POSTGRES_CONTAINER =
            (PostgreSQLContainer) new PostgreSQLContainer("postgres:14-alpine")
                    .withUsername("rwx_user")
                    .withPassword(PASSWORD);

    @Container
    private static final MariaDBContainer<?> MARIA_DB_CONTAINER =
            new MariaDBContainer<>("mariadb:10.6.1")
                    .withEnv("MARIADB_ROOT_PASSWORD", PASSWORD);

    @Autowired
    private MigrationService migrationService;

    @Autowired
    private SqlDataSourceProvider sqlDataSourceProvider;

    @Autowired
    private SqlDataSourceFactory sqlDataSourceFactory;

    public static Stream<DataSourceSpec> testDataSourceSpec() {
        return Stream.of(
                new DomainDataSourceSpec("SQLSERVER", RWX),
                new DomainDataSourceSpec("POSTGRES", RWX),
                new DomainDataSourceSpec("MARIADB", RWX)
        );
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (DataSourceSpec dataSourceSpec : testDataSourceSpec().toList()) {
            JpaDataSourceSpec jpaDataSourceSpec = DomainJpaSpecDataSource.builder()
                    .jpaSpec(SYSTEM_JPA_SPEC)
                    .dataSourceName(dataSourceSpec.getDataSourceName())
                    .permission(dataSourceSpec.getPermission())
                    .build();
            DataSource dataSource = sqlDataSourceProvider.findDestination(jpaDataSourceSpec)
                    .map(sqlDataSource -> sqlDataSourceFactory.fabricate(sqlDataSource, RWX))
                    .orElseThrow();
            migrationService.migrateAsync(dataSource).get();
        }
    }

    @BeforeAll
    static void setupDataBase() throws SQLException {
        createSQLServerTestEnv();
        createPostgresEnv();
    }

    @DynamicPropertySource
    static void setDataSourceProperties(DynamicPropertyRegistry registry) {
        String prefix = "spring.application.data-source.instance";
        registry.add(prefix + ".0.name", () -> SQL_SERVER_DATASOURCE);
        registry.add(prefix + ".0.vendor", SQL_SERVER::name);
        registry.add(prefix + ".0.host", SQL_SERVER_CONTAINER::getHost);
        registry.add(prefix + ".0.port", () -> SQL_SERVER_CONTAINER.getMappedPort(1433));
        registry.add(prefix + ".0.database", () -> DATABASE);
        registry.add(prefix + ".0.user.0.schema", () -> "warehouse_index");
        registry.add(prefix + ".0.user.0.read-write-execute.username", SQL_SERVER_CONTAINER::getUsername);
        registry.add(prefix + ".0.user.0.read-write-execute.password", SQL_SERVER_CONTAINER::getPassword);
        registry.add(prefix + ".0.user.1.schema", () -> "dbo");
        registry.add(prefix + ".0.user.1.read-write-execute.username", SQL_SERVER_CONTAINER::getUsername);
        registry.add(prefix + ".0.user.1.read-write-execute.password", SQL_SERVER_CONTAINER::getPassword);
        registry.add(prefix + ".0.property.trustServerCertificate", () -> "true");
        registry.add(prefix + ".0.property.encrypt", () -> "false");
        registry.add(prefix + ".1.name", () -> POSTGRES_DATASOURCE);
        registry.add(prefix + ".1.vendor", POSTGRESQL::name);
        registry.add(prefix + ".1.host", POSTGRES_CONTAINER::getHost);
        registry.add(prefix + ".1.port", () -> POSTGRES_CONTAINER.getMappedPort(5432));
        registry.add(prefix + ".1.database", () -> DATABASE);
        registry.add(prefix + ".1.user.0.schema", () -> "warehouse_index");
        registry.add(prefix + ".1.user.0.read-write-execute.username", POSTGRES_CONTAINER::getUsername);
        registry.add(prefix + ".1.user.0.read-write-execute.password", POSTGRES_CONTAINER::getPassword);
        registry.add(prefix + ".1.user.1.schema", () -> "public");
        registry.add(prefix + ".1.user.1.read-write-execute.username", POSTGRES_CONTAINER::getUsername);
        registry.add(prefix + ".1.user.1.read-write-execute.password", POSTGRES_CONTAINER::getPassword);
        registry.add(prefix + ".2.name", () -> MARIADB_DATASOURCE);
        registry.add(prefix + ".2.vendor", MARIADB::name);
        registry.add(prefix + ".2.host", MARIA_DB_CONTAINER::getHost);
        registry.add(prefix + ".2.port", () -> MARIA_DB_CONTAINER.getMappedPort(3306));
        registry.add(prefix + ".2.database", MARIA_DB_CONTAINER::getDatabaseName);
        registry.add(prefix + ".2.user.0.schema", () -> "warehouse_index");
        registry.add(prefix + ".2.user.0.read-write-execute.username", () -> "root");
        registry.add(prefix + ".2.user.0.read-write-execute.password", () -> PASSWORD);
        registry.add(prefix + ".2.user.1.schema", () -> "mysql");
        registry.add(prefix + ".2.user.1.read-write-execute.username", () -> "root");
        registry.add(prefix + ".2.user.1.read-write-execute.password", () -> PASSWORD);
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
