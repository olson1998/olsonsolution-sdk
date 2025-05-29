package com.olsonsolution.common.spring.application.migration.config;

import com.olsonsolution.common.liquibase.domain.service.datasource.MariaDBSqlVendorSupporter;
import com.olsonsolution.common.liquibase.domain.service.datasource.PostgresqlVendorSupporter;
import com.olsonsolution.common.liquibase.domain.service.datasource.SqlServerVendorSupporter;
import com.olsonsolution.common.migration.domain.port.repository.SqlVendorSupporter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.olsonsolution.common.spring.application.jpa.config.DataSourceModelersConfig.ENABLED_VALUE;
import static com.olsonsolution.common.spring.application.jpa.config.DataSourceModelersConfig.SPRING_APPLICATION_JPA_DATA_SOURCE_MODELERS_PROPERTIES_PREFIX;

@Configuration
public class SqlVendorSupportersConfig {

    @Bean
    @ConditionalOnProperty(
            value = SPRING_APPLICATION_JPA_DATA_SOURCE_MODELERS_PROPERTIES_PREFIX + ".sql-server",
            havingValue = ENABLED_VALUE
    )
    public SqlVendorSupporter sqlServerVendorSupporter() {
        return new SqlServerVendorSupporter();
    }

    @Bean
    @ConditionalOnProperty(
            value = SPRING_APPLICATION_JPA_DATA_SOURCE_MODELERS_PROPERTIES_PREFIX + ".postgresql",
            havingValue = ENABLED_VALUE
    )
    public SqlVendorSupporter postgresqlVendorSupporter() {
        return new PostgresqlVendorSupporter();
    }

    @Bean
    @ConditionalOnProperty(
            value = SPRING_APPLICATION_JPA_DATA_SOURCE_MODELERS_PROPERTIES_PREFIX + ".mariadb",
            havingValue = ENABLED_VALUE
    )
    public SqlVendorSupporter mariaDbSqlVendorSupporter() {
        return new MariaDBSqlVendorSupporter();
    }

}
