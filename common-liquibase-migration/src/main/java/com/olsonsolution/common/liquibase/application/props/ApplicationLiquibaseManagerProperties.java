package com.olsonsolution.common.liquibase.application.props;

import com.olsonsolution.common.liquibase.domain.port.props.LiquibaseManagerProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static com.olsonsolution.common.liquibase.application.props.ApplicationLiquibaseManagerProperties.LIQUIBASE_MANAGER_PROPERTIES_PREFIX;
import static liquibase.UpdateSummaryEnum.SUMMARY;

@Data
@Configuration
@ConfigurationProperties(prefix = LIQUIBASE_MANAGER_PROPERTIES_PREFIX)
public class ApplicationLiquibaseManagerProperties implements LiquibaseManagerProperties {

    public static final String LIQUIBASE_MANAGER_PROPERTIES_PREFIX = "spring.application.liquibase";

    private liquibase.UpdateSummaryEnum updateSummary = SUMMARY;

    private final ExecutorProperties executor = new ExecutorProperties();

    @Data
    public static class ExecutorProperties {

        private int threads = 5;

        private int queueSize = 30;

    }

}
