package com.olsonsolution.common.spring.application.jpa.props;

import com.olsonsolution.common.spring.domain.port.props.jpa.JpaSpecProperties;
import com.olsonsolution.common.spring.domain.port.props.jpa.JpaProperties;
import com.olsonsolution.common.spring.domain.port.props.jpa.PackagesToScanProperties;
import com.olsonsolution.common.spring.domain.port.props.jpa.RoutingDataSourceProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.*;

import static com.olsonsolution.common.spring.application.jpa.props.SpringApplicationJpaProperties.SPRING_APPLICATION_JPA_PROPERTIES_PREFIX;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = SPRING_APPLICATION_JPA_PROPERTIES_PREFIX)
public class SpringApplicationJpaProperties implements JpaProperties {

    public static final String SPRING_APPLICATION_PROPERTIES_PREFIX = "spring.application";

    public static final String SPRING_APPLICATION_JPA_PROPERTIES_PREFIX = SPRING_APPLICATION_PROPERTIES_PREFIX + ".jpa";

    private final Map<String, String> dataSourceModeler = new HashMap<>();
    private final List<ApplicationJpaSpecProperties> config = new ArrayList<>();
    private final ApplicationRoutingDataSourceProperties routingDataSource = new ApplicationRoutingDataSourceProperties();

    @Override
    public Map<String, String> getDataSourceModelersEnableProperties() {
        return dataSourceModeler;
    }

    @Override
    public RoutingDataSourceProperties getRoutingDataSourceProperties() {
        return routingDataSource;
    }

    @Override
    public Collection<? extends JpaSpecProperties> getJpaSpecificationsProperties() {
        return config;
    }

    @Data
    public static class ApplicationRoutingDataSourceProperties implements RoutingDataSourceProperties {

        private int maxDataSources = 50;

        private Duration expireTimeout = Duration.ofHours(4);

    }

    @Data
    public static class ApplicationJpaSpecProperties implements JpaSpecProperties {

        private String name = "Jpa";

        private String schema;

        private boolean logSql;

        private boolean formatSqlLog;

        private final Properties properties = new Properties();

        private final ApplicationPackagesToScanProperties entity = new ApplicationPackagesToScanProperties();

        private final ApplicationPackagesToScanProperties repository = new ApplicationPackagesToScanProperties();

        @Override
        public PackagesToScanProperties getEntityProperties() {
            return entity;
        }

        @Override
        public PackagesToScanProperties getJpaRepositoryProperties() {
            return repository;
        }

        @Data
        public static class ApplicationPackagesToScanProperties implements PackagesToScanProperties {

            private final Set<String> packagesToScan = new HashSet<>();

        }

    }

}
