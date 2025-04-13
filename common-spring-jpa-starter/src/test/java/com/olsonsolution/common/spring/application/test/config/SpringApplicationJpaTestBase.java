package com.olsonsolution.common.spring.application.test.config;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import com.olsonsolution.common.spring.application.caching.InMemoryCachingConfig;
import com.olsonsolution.common.spring.application.jpa.config.DataSourceModelersConfig;
import com.olsonsolution.common.spring.application.jpa.config.DataSourceSpecConfig;
import com.olsonsolution.common.spring.application.jpa.config.JpaSpecConfigurer;
import com.olsonsolution.common.spring.application.jpa.config.SqlVendorPropertiesResolverConfig;
import com.olsonsolution.common.spring.application.jpa.props.SpringApplicationDestinationDataSourceProperties;
import com.olsonsolution.common.spring.application.jpa.props.SpringApplicationJpaProperties;
import com.olsonsolution.common.spring.domain.model.datasource.DataSourceSpecification;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.olsonsolution.common.data.domain.model.sql.SqlPermissions.RWX;
import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.POSTGRESQL;
import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.SQL_SERVER;
import static com.olsonsolution.common.spring.application.jpa.config.DataSourceModelersConfig.SPRING_APPLICATION_JPA_DATA_SOURCE_MODELERS_PROPERTIES_PREFIX;
import static com.olsonsolution.common.spring.application.jpa.props.SpringApplicationDestinationDataSourceProperties.SPRING_APPLICATION_JPA_DESTINATION_DATA_SOURCE_PROPERTIES_PREFIX;
import static com.olsonsolution.common.spring.application.jpa.props.SpringApplicationJpaProperties.SPRING_APPLICATION_JPA_PROPERTIES_PREFIX;
import static com.olsonsolution.common.spring.application.test.config.SpringApplicationJpaTestBase.*;

@EnableTransactionManagement
@EnableConfigurationProperties
@ContextConfiguration(classes = {
        DataSourceSpecConfig.class,
        DataSourceModelersConfig.class,
        InMemoryCachingConfig.class,
        SqlVendorPropertiesResolverConfig.class,
        JpaSpecConfigurer.class,
        SpringApplicationJpaProperties.class,
        SpringApplicationDestinationDataSourceProperties.class
})
@ComponentScan(basePackages = "com.olsonsolution.common.spring.application.config.jpa.test")
@ExtendWith(SpringExtension.class)
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
        SPRING_APPLICATION_JPA_DATA_SOURCE_MODELERS_PROPERTIES_PREFIX + ".sql-server=enabled",
        SPRING_APPLICATION_JPA_DATA_SOURCE_MODELERS_PROPERTIES_PREFIX + ".postgresql=enabled",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.0.name=Classic",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.0.schema=company",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.0.log-sql=true",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.0.format-sql-log=true",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.0.entity.packages-to-scan.0=" + CLASSIC_ENTITY_PACKAGE,
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.0.repository.packages-to-scan.0=" + CLASSIC_REPO_PACKAGE,
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.1.name=Modern",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.1.schema=company_structure",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.1.log-sql=true",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.1.format-sql-log=true",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.1.entity.packages-to-scan.0=" + ENTITY_PACKAGE,
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.1.repository.packages-to-scan.0=" + REPO_PACKAGE,
        "spring.jpa.show-sql=true"
})
public abstract class SpringApplicationJpaTestBase {

    protected static final String DATABASE = "unit_test";

    protected static final String PASSWORD = "My$uperSeccretPass@@@";

    protected static final String SQL_SERVER_DATASOURCE = "SQLSERVER";

    protected static final String POSTGRES_DATASOURCE = "POSTGRES";

    protected static final String ENTITY_PACKAGE =
            "com.olsonsolution.common.spring.application.datasource.modern.entity";

    protected static final String REPO_PACKAGE =
            "com.olsonsolution.common.spring.application.datasource.modern.repository";

    protected static final String CLASSIC_ENTITY_PACKAGE =
            "com.olsonsolution.common.spring.application.datasource.classic.entity";

    protected static final String CLASSIC_REPO_PACKAGE =
            "com.olsonsolution.common.spring.application.datasource.classic.repository";

    private static final String CREATE_PERSON_TABLE_SQL_SERVER_QUERY = """
            CREATE TABLE company_structure.person (
                id BIGINT PRIMARY KEY,
                name NVARCHAR(255),
                surname NVARCHAR(255),
                gender NVARCHAR(255)
            );
            """;
    private static final String CREATE_TEAM_SQL_SERVER_QUERY = """
            CREATE TABLE company_structure.team (
                id BIGINT PRIMARY KEY,
                code NVARCHAR(255),
                name NVARCHAR(255)
            );
            """;
    private static final String CREATE_PERSON_TEAM_BOUND_SQL_SERVER_QUERY = """
            CREATE TABLE company_structure.person_team_bound (
                team_id BIGINT NOT NULL,
                person_id BIGINT NOT NULL,
                PRIMARY KEY (team_id, person_id)
            );
            """;
    private static final String CREATE_CLASSIC_PERSON_SQL_SERVER_QUERY = """
            CREATE TABLE COMPANY.PRSDTA (
                PRSID BIGINT PRIMARY KEY,
                PRSNM NVARCHAR(255),
                PRSSN NVARCHAR(255),
                PRSGN NVARCHAR(255)
            );
            """;
    private static final String CREATE_CLASSIC_TEAM_SQL_SERVER_QUERY = """
            CREATE TABLE COMPANY.TMMDTA (
                TMMID BIGINT PRIMARY KEY,
                TMMCD NVARCHAR(255),
                TMMNM NVARCHAR(255)
            );
            """;
    private static final String CREATE_CLASSIC_PERSON_TEAM_BOUND_SQL_SERVER_QUERY = """
            CREATE TABLE COMPANY.PTMBND (
                PRSID BIGINT,
                TMMID BIGINT
                PRIMARY KEY (PRSID, TMMID)
            );
            """;

    private static final String CREATE_PERSON_TABLE_POSTGRES_QUERY = """
            CREATE TABLE company_structure.person (
                id BIGSERIAL PRIMARY KEY,
                name VARCHAR(255),
                surname VARCHAR(255),
                gender VARCHAR(255)
            );
            """;

    private static final String CREATE_TEAM_TABLE_POSTGRES_QUERY = """
            CREATE TABLE company_structure.team (
                id BIGSERIAL PRIMARY KEY,
                code VARCHAR(255),
                name VARCHAR(255)
            );
            """;

    private static final String CREATE_PERSON_TEAM_BOUND_POSTGRES_QUERY = """
            CREATE TABLE company_structure.person_team_bound (
                team_id BIGINT NOT NULL,
                person_id BIGINT NOT NULL,
                PRIMARY KEY (team_id, person_id),
                FOREIGN KEY (team_id) REFERENCES company_structure.team (id),
                FOREIGN KEY (person_id) REFERENCES company_structure.person (id)
            );
            """;

    private static final String CREATE_CLASSIC_PERSON_POSTGRES_QUERY = """
            CREATE TABLE company.prsdta (
                prsid BIGSERIAL PRIMARY KEY,
                prsnm VARCHAR(255),
                prssn VARCHAR(255),
                prsgn VARCHAR(255)
            );
            """;

    private static final String CREATE_CLASSIC_TEAM_POSTGRES_QUERY = """
            CREATE TABLE company.tmmdta (
                tmmid BIGSERIAL PRIMARY KEY,
                tmmcd VARCHAR(255),
                tmmnm VARCHAR(255)
            );
            """;

    private static final String CREATE_CLASSIC_PERSON_TEAM_BOUND_POSTGRES_QUERY = """
            CREATE TABLE company.ptmbnd (
                prsid BIGINT NOT NULL,
                tmmid BIGINT NOT NULL,
                PRIMARY KEY (prsid, tmmid),
                FOREIGN KEY (prsid) REFERENCES company.prsdta (prsid),
                FOREIGN KEY (tmmid) REFERENCES company.tmmdta (tmmid)
            );
            """;

    private static final List<String> SQL_SERVER_QUERIES = Stream.of(
            "CREATE SCHEMA company_structure",
            "CREATE SCHEMA COMPANY",
            CREATE_CLASSIC_PERSON_SQL_SERVER_QUERY,
            CREATE_CLASSIC_TEAM_SQL_SERVER_QUERY,
            CREATE_CLASSIC_PERSON_TEAM_BOUND_SQL_SERVER_QUERY,
            CREATE_PERSON_TABLE_SQL_SERVER_QUERY,
            CREATE_TEAM_SQL_SERVER_QUERY,
            CREATE_PERSON_TEAM_BOUND_SQL_SERVER_QUERY
    ).collect(Collectors.toCollection(LinkedList::new));

    private static final List<String> POSTGRES_QUERIES = Stream.of(
            "CREATE SCHEMA company_structure",
            "CREATE SCHEMA company",
            CREATE_CLASSIC_PERSON_POSTGRES_QUERY,
            CREATE_CLASSIC_TEAM_POSTGRES_QUERY,
            CREATE_CLASSIC_PERSON_TEAM_BOUND_POSTGRES_QUERY,
            CREATE_PERSON_TABLE_POSTGRES_QUERY,
            CREATE_TEAM_TABLE_POSTGRES_QUERY,
            CREATE_PERSON_TEAM_BOUND_POSTGRES_QUERY
    ).collect(Collectors.toCollection(LinkedList::new));

    @Container
    private static final MSSQLServerContainer<?> SQL_SERVER_CONTAINER = new MSSQLServerContainer<>()
            .withPassword(PASSWORD)
            .acceptLicense();

    @Container
    private static final PostgreSQLContainer POSTGRES_CONTAINER =
            new PostgreSQLContainer("postgres:14-alpine")
                    .withUsername("sa")
                    .withPassword(PASSWORD);

    public static Stream<DataSourceSpec> dataSourceSpecStream() {
        return Stream.of(
                new DataSourceSpecification(SQL_SERVER_DATASOURCE, RWX),
                new DataSourceSpecification(POSTGRES_DATASOURCE, RWX)
        );
    }

    @BeforeAll
    static void setupDataSources() throws SQLException {
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
    }

    private static void createSQLServerTestEnv() throws SQLServerException, SQLException {
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
        sqlServerDataSource = new SQLServerDataSource();
        sqlServerDataSource.setServerName(host);
        sqlServerDataSource.setPortNumber(port);
        sqlServerDataSource.setUser(username);
        sqlServerDataSource.setPassword(password);
        sqlServerDataSource.setDatabaseName(DATABASE);
        sqlServerDataSource.setEncrypt("false");
        sqlServerDataSource.setTrustServerCertificate(true);
        try (Connection connection = sqlServerDataSource.getConnection()) {
            for (String sql : SQL_SERVER_QUERIES) {
                try (PreparedStatement query = connection.prepareStatement(sql)) {
                    query.execute();
                }
            }
        }
    }

    private static void createPostgresEnv() throws SQLServerException, SQLException {
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
        postgresDataSource = new PGSimpleDataSource();
        postgresDataSource.setServerNames(new String[]{host});
        postgresDataSource.setPortNumbers(new int[]{port});
        postgresDataSource.setDatabaseName(DATABASE);
        postgresDataSource.setUser(username);
        postgresDataSource.setPassword(password);
        try (Connection connection = postgresDataSource.getConnection()) {
            for (String sql : POSTGRES_QUERIES) {
                try (PreparedStatement query = connection.prepareStatement(sql)) {
                    query.execute();
                }
            }
        }
    }

}
