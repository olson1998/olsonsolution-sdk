package com.olsonsolution.common.liquibase.application.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "")
public class LiquibaseMigrationProperties {

    private final ExecutorProperties executor = new ExecutorProperties();

    @Data
    public static class ExecutorProperties {


    }

}
