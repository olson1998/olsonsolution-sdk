package com.olsonsolution.common.spring.application.async.props;

import com.olsonsolution.common.spring.domain.port.async.props.ThreadPoolProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static com.olsonsolution.common.spring.application.async.props.AsyncProperties.ASYNC_PROPERTIES_BEAN;

@Data
@Configuration(ASYNC_PROPERTIES_BEAN)
@ConfigurationProperties(prefix = "spring.application.async")
public class AsyncProperties {

    public static final String ASYNC_PROPERTIES_BEAN = "asyncProperties";

    private final ExecutorProperties system = new ExecutorProperties(
            "system",
            "system",
            new ExecutorProperties.PoolProperties(10, 50, 100, Duration.ofSeconds(30))
    );

    private final ExecutorProperties runtime = new ExecutorProperties(
            "async",
            "runtime",
            new ExecutorProperties.PoolProperties(30, 50, 100, Duration.ofSeconds(30))
    );

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutorProperties {

        private String prefix;

        private String group;

        private PoolProperties threadPool;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PoolProperties implements ThreadPoolProperties {

            private int size;

            private int maxPoolSize;

            private int queueCapacity;

            private Duration keepAlive;

        }

    }

}
