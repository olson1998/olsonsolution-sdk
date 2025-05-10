package com.olsonsolution.common.spring.application.test.config;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResults;
import com.olsonsolution.common.spring.application.migration.config.LiquibaseConfig;
import com.olsonsolution.common.spring.application.migration.config.SqlVendorSupportersConfig;
import com.olsonsolution.common.spring.application.migration.props.LiquibaseProperties;
import com.olsonsolution.common.migration.domain.port.repository.MigrationService;
import com.olsonsolution.common.spring.application.caching.InMemoryCachingConfig;
import com.olsonsolution.common.spring.application.config.time.TimeUtilsConfig;
import com.olsonsolution.common.spring.application.jpa.config.DataSourceModelersConfig;
import com.olsonsolution.common.spring.application.jpa.config.DataSourceSpecConfig;
import com.olsonsolution.common.spring.application.jpa.config.JpaSpecConfigurer;
import com.olsonsolution.common.spring.application.jpa.config.SqlVendorPropertiesResolverConfig;
import com.olsonsolution.common.spring.application.jpa.props.SpringApplicationDestinationDataSourceProperties;
import com.olsonsolution.common.spring.application.jpa.props.SpringApplicationJpaProperties;
import com.olsonsolution.common.spring.application.migration.config.ChangeLogProviderConfig;
import com.olsonsolution.common.spring.application.props.time.JodaDateTimeProperties;
import com.olsonsolution.common.spring.domain.model.datasource.DataSourceSpecification;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.olsonsolution.common.data.domain.model.sql.SqlPermissions.RWX;
import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.POSTGRESQL;
import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.SQL_SERVER;
import static com.olsonsolution.common.spring.application.jpa.config.DataSourceModelersConfig.SPRING_APPLICATION_JPA_DATA_SOURCE_MODELERS_PROPERTIES_PREFIX;
import static com.olsonsolution.common.spring.application.jpa.props.SpringApplicationDestinationDataSourceProperties.SPRING_APPLICATION_JPA_DESTINATION_DATA_SOURCE_PROPERTIES_PREFIX;
import static com.olsonsolution.common.spring.application.jpa.props.SpringApplicationJpaProperties.SPRING_APPLICATION_JPA_PROPERTIES_PREFIX;
import static com.olsonsolution.common.spring.application.test.config.SpringApplicationJpaTestBase.*;
import static org.assertj.core.api.Assertions.assertThat;

@EnableTransactionManagement
@EnableConfigurationProperties
@ContextConfiguration(classes = {
        TimeUtilsConfig.class,
        ChangeLogProviderConfig.class,
        DataSourceSpecConfig.class,
        DataSourceModelersConfig.class,
        SqlVendorSupportersConfig.class,
        LiquibaseConfig.class,
        InMemoryCachingConfig.class,
        SqlVendorPropertiesResolverConfig.class,
        JpaSpecConfigurer.class,
        JodaDateTimeProperties.class,
        LiquibaseProperties.class,
        SpringApplicationJpaProperties.class,
        SpringApplicationDestinationDataSourceProperties.class
})
@ComponentScan(basePackages = "com.olsonsolution.common.spring.application.config.jpa.test")
@ExtendWith(SpringExtension.class)
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
        SPRING_APPLICATION_JPA_DATA_SOURCE_MODELERS_PROPERTIES_PREFIX + ".sql-server=enabled",
        SPRING_APPLICATION_JPA_DATA_SOURCE_MODELERS_PROPERTIES_PREFIX + ".postgresql=enabled",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.0.name=Membership",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.0.schema=membership",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.0.log-sql=true",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.0.format-sql-log=true",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.0.entity.packages-to-scan.0=" + CLASSIC_ENTITY_PACKAGE,
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.0.repository.packages-to-scan.0=" + CLASSIC_REPO_PACKAGE,
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.1.name=Person",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.1.schema=person",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.1.log-sql=true",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.1.format-sql-log=true",
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.1.entity.packages-to-scan.0=" + ENTITY_PACKAGE,
        SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".config.1.repository.packages-to-scan.0=" + REPO_PACKAGE,
        "spring.jpa.show-sql=true"
})
public abstract class SpringApplicationJpaTestBase implements InitializingBean {

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

    @Container
    private static final MSSQLServerContainer<?> SQL_SERVER_CONTAINER = new MSSQLServerContainer<>()
            .withPassword(PASSWORD)
            .acceptLicense();

    @Container
    private static final PostgreSQLContainer POSTGRES_CONTAINER =
            new PostgreSQLContainer("postgres:14-alpine")
                    .withUsername("sa")
                    .withPassword(PASSWORD);

    @Autowired
    private MigrationService migrationService;

    @Autowired
    private DataSourceSpecManager dataSourceSpecManager;

    @Autowired
    private DestinationDataSourceManager destinationDataSourceManager;

    public static Stream<DataSourceSpec> dataSourceSpecStream() {
        return Stream.of(
                new DataSourceSpecification(SQL_SERVER_DATASOURCE, RWX),
                new DataSourceSpecification(POSTGRES_DATASOURCE, RWX)
        );
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (DataSourceSpec dataSourceSpec : dataSourceSpecStream().toList()) {
            dataSourceSpecManager.setThreadLocal(dataSourceSpec);
            DataSource dataSource = destinationDataSourceManager.selectDataSourceBySpec(dataSourceSpec);
            MigrationResults migrationResults = migrationService.migrateAsync(dataSource)
                    .get(30, TimeUnit.SECONDS);
            dataSourceSpecManager.clearThreadLocal();
            assertThat(migrationResults.getFailed()).isZero();
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
    }


}
