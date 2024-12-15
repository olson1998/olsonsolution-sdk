package com.olsonsolution.common.spring.application.jpa.props;

import com.olsonsolution.common.spring.domain.model.datasource.DataSourceSpecification;
import com.olsonsolution.common.spring.domain.port.props.jpa.*;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.*;

@Data
@Configuration
@ConfigurationProperties(prefix = "common.spring.application.jpa")
public class ApplicationJpaProperties implements JpaProperties {

    private final ApplicationDefaultDataSourceProperties defaultDataSource = new ApplicationDefaultDataSourceProperties();
    private final ApplicationRoutingDataSourceProperties routingDataSource = new ApplicationRoutingDataSourceProperties();
    private final List<ApplicationEntityManagerFactoryProperties> entityManagerFactory = new ArrayList<>();

    @Override
    public DefaultDataSourceSpecProperties getDefaultDataSourceProperties() {
        return defaultDataSource;
    }

    @Override
    public RoutingDataSourceProperties getRoutingDataSourceProperties() {
        return routingDataSource;
    }

    @Override
    public Collection<? extends EntityManagerFactoryProperties> getEntityManagerFactoryProperties() {
        return entityManagerFactory;
    }

    @Data
    public static class ApplicationDefaultDataSourceProperties implements DefaultDataSourceSpecProperties {

        private final DataSourceSpecification specification = new DataSourceSpecification();

        @Override
        public DataSourceSpec getSpecProperties() {
            return specification;
        }
    }

    @Data
    public static class ApplicationRoutingDataSourceProperties implements RoutingDataSourceProperties {

        private int maxDataSources = 50;

        private Duration expireTimeout = Duration.ofHours(4);

    }

    @Data
    public static class ApplicationEntityManagerFactoryProperties implements EntityManagerFactoryProperties {

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
