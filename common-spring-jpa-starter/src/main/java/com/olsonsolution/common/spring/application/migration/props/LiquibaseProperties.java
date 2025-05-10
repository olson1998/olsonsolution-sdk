package com.olsonsolution.common.spring.application.migration.props;

import com.olsonsolution.common.migration.domain.port.repository.VariablesProvider;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.application.liquibase")
public class LiquibaseProperties implements VariablesProvider {

    private String author = "application";
    private final ExecutorProperties executor = new ExecutorProperties();

    @Override
    public Map<String, String> getVariables() {
        return Collections.singletonMap("author", author);
    }

    @Data
    public static class ExecutorProperties {

        private int poolSize = 10;

        private int queueCapacity = 50;

        private Duration terminationTimeout = Duration.ofSeconds(5);

        private Duration executionTimeout = Duration.ofSeconds(30);

    }

}
