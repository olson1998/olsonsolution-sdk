package com.olsonsolution.common.spring.application.async.config;

import com.olsonsolution.common.spring.application.async.props.AsyncProperties;
import com.olsonsolution.common.spring.domain.port.async.repository.ExecutorFactory;
import com.olsonsolution.common.spring.domain.port.async.repository.ThreadLocalAware;
import com.olsonsolution.common.spring.domain.port.async.repository.ThreadLocalAwareTaskRunner;
import com.olsonsolution.common.spring.domain.service.async.ExecutorFabricatingService;
import com.olsonsolution.common.spring.domain.service.async.ThreadLocalAwareTaskRunningService;
import com.olsonsolution.common.spring.domain.service.async.ThreadLocalAwareTaskThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

@Configuration
public class AsyncConfig {

    public static final String SYSTEM_EXECUTOR_BEAN = "systemExecutor";

    public static final String RUNTIME_EXECUTOR_BEAN = "runtimeExecutor";

    @Bean
    public ExecutorFactory executorFactory() {
        return new ExecutorFabricatingService();
    }

    @Bean
    public ThreadLocalAwareTaskRunner threadLocalAwareTaskRunner(List<ThreadLocalAware<?>> threadLocalAwares) {
        return new ThreadLocalAwareTaskRunningService(threadLocalAwares);
    }

    @Bean(SYSTEM_EXECUTOR_BEAN)
    public Executor systemExecutor(ExecutorFactory executorFactory,
                                   AsyncProperties asyncProperties,
                                   ThreadLocalAwareTaskRunner threadLocalAwareTaskRunner) {
        AsyncProperties.ExecutorProperties executorProperties = asyncProperties.getSystem();
        ThreadFactory threadFactory = new ThreadLocalAwareTaskThreadFactory(
                executorProperties.getPrefix(),
                threadLocalAwareTaskRunner
        );
        return executorFactory.fabricate(asyncProperties.getSystem().getThreadPool(), threadFactory);
    }

    @Bean(RUNTIME_EXECUTOR_BEAN)
    public Executor runtimeExecutor(ExecutorFactory executorFactory,
                                    AsyncProperties asyncProperties,
                                    ThreadLocalAwareTaskRunner threadLocalAwareTaskRunner) {
        AsyncProperties.ExecutorProperties executorProperties = asyncProperties.getRuntime();
        ThreadFactory threadFactory = new ThreadLocalAwareTaskThreadFactory(
                executorProperties.getPrefix(),
                threadLocalAwareTaskRunner
        );
        return executorFactory.fabricate(asyncProperties.getSystem().getThreadPool(), threadFactory);
    }

}
