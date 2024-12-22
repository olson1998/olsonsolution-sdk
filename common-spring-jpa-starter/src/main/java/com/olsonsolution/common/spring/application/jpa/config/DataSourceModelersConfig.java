package com.olsonsolution.common.spring.application.jpa.config;

import com.olsonsolution.common.data.domain.port.repository.sql.DataSourceFactory;
import com.olsonsolution.common.data.domain.port.repository.sql.DataSourceModeler;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.data.domain.service.sql.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;

import static com.olsonsolution.common.spring.application.jpa.props.SpringApplicationJpaProperties.SPRING_APPLICATION_JPA_PROPERTIES_PREFIX;

@Slf4j
@Configuration
public class DataSourceModelersConfig {

    public static final String ENABLED_VALUE = "enabled";

    public static final String SPRING_APPLICATION_JPA_DATA_SOURCE_MODELERS_PROPERTIES_PREFIX =
            SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".data-source-modeler";

    @Bean
    @ConditionalOnProperty(
            value = SPRING_APPLICATION_JPA_DATA_SOURCE_MODELERS_PROPERTIES_PREFIX + ".sql-server",
            havingValue = ENABLED_VALUE
    )
    public DataSourceModeler sqlServerDataSourceModeler() {
        return new SqlServerDataSourceModeler();
    }

    @Bean
    @ConditionalOnProperty(
            value = SPRING_APPLICATION_JPA_DATA_SOURCE_MODELERS_PROPERTIES_PREFIX + ".postgresql",
            havingValue = ENABLED_VALUE
    )
    public DataSourceModeler posgresqlDataSourceModeler() {
        return new PostgresDataSourceModeler();
    }

    @Bean
    @ConditionalOnProperty(
            value = SPRING_APPLICATION_JPA_DATA_SOURCE_MODELERS_PROPERTIES_PREFIX + ".mariadb",
            havingValue = ENABLED_VALUE
    )
    public DataSourceModeler mariadbDataSourceModeler() {
        return new MariaDbDataSourceModeler();
    }

    @Bean
    @ConditionalOnProperty(
            value = SPRING_APPLICATION_JPA_DATA_SOURCE_MODELERS_PROPERTIES_PREFIX + ".db2",
            havingValue = ENABLED_VALUE
    )
    public DataSourceModeler db2DataSourceModeler() {
        return new Db2DataSourceModeler();
    }

    @Bean
    public DataSourceFactory dataSourceFactory(List<DataSourceModeler> dataSourceModelers) {
        Collection<SqlVendor> enabledVendors = dataSourceModelers.stream()
                .map(DataSourceModeler::getSqlVendor)
                .toList();
        if(enabledVendors.isEmpty()) {
            throw new IllegalStateException("No Data Source Modeler registered");
        }
        log.info("Data Source Modelers enabled for vendors: {}", enabledVendors);
        return new DataSourceFabricatingService(dataSourceModelers);
    }

}
