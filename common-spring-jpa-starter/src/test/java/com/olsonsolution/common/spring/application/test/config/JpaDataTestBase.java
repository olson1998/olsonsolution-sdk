package com.olsonsolution.common.spring.application.test.config;

import com.olsonsolution.common.spring.application.caching.InMemoryCachingConfig;
import com.olsonsolution.common.spring.application.jpa.config.DataSourceSpecConfig;
import com.olsonsolution.common.spring.application.jpa.config.RoutingJpaConfigurer;
import com.olsonsolution.common.spring.application.jpa.props.ApplicationJpaProperties;
import com.olsonsolution.common.spring.application.jpa.props.DestinationDataSourceProperties;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.Db2Container;
import org.testcontainers.containers.MSSQLServerContainer;

import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.DB2;
import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.SQL_SERVER;

@EnableConfigurationProperties
@ContextConfiguration(classes = {
        DataSourceSpecConfig.class,
        RoutingJpaConfigurer.class,
        InMemoryCachingConfig.class,
        ApplicationJpaProperties.class,
        DestinationDataSourceProperties.class
})
@TestPropertySource(locations = "classpath://application-spring-common.properties")
@ExtendWith(SpringExtension.class)
@TestContainers
public abstract class JpaDataTestBase {

    protected static final String DATABASE = "UNITTEST";

    protected static final String PASSWORD = "pass";

    protected static final String SQL_SERVER_DATASOURCE = "SQLSERVER";

    protected static final String DB2_DATASOURCE = "DB2";

    private static final MSSQLServerContainer<?> SQL_SERVER_CONTAINER = new MSSQLServerContainer<>()
            .withDatabaseName(DATABASE)
            .withPassword(PASSWORD);

    private static final Db2Container DB2_CONTAINER = new Db2Container()
            .withDatabaseName(DATABASE)
            .withPassword(PASSWORD);

    @DynamicPropertySource
    static void setDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("common.spring.application.jpa.routing.instance.0.name", () -> SQL_SERVER_DATASOURCE);
        registry.add("common.spring.application.jpa.routing.instance.0.datasource.vendor", SQL_SERVER::name);
        registry.add(
                "common.spring.application.jpa.routing.instance.0.datasource.host",
                SQL_SERVER_CONTAINER::getHost
        );
        registry.add(
                "common.spring.application.jpa.routing.instance.0.datasource.port",
                SQL_SERVER_CONTAINER::getFirstMappedPort
        );
        registry.add(
                "common.spring.application.jpa.routing.instance.0.datasource.users.read-write-execute.0.username",
                SQL_SERVER_CONTAINER::getUsername
        );
        registry.add(
                "common.spring.application.jpa.routing.instance.0.datasource.users.read-write-execute.0.password",
                SQL_SERVER_CONTAINER::getPassword
        );
        registry.add("common.spring.application.jpa.routing.instance.1.name", () -> DB2_DATASOURCE);
        registry.add("common.spring.application.jpa.routing.instance.1.datasource.vendor", DB2::name);
        registry.add(
                "common.spring.application.jpa.routing.instance.1.datasource.host",
                DB2_CONTAINER::getHost
        );
        registry.add(
                "common.spring.application.jpa.routing.instance.1.datasource.port",
                DB2_CONTAINER::getFirstMappedPort
        );
        registry.add(
                "common.spring.application.jpa.routing.instance.1.datasource.users.read-write-execute.0.username",
                DB2_CONTAINER::getUsername
        );
        registry.add(
                "common.spring.application.jpa.routing.instance.1.datasource.users.read-write-execute.0.password",
                DB2_CONTAINER::getPassword
        );
    }
}
