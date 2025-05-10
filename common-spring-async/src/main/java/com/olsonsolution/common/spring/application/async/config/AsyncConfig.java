package com.olsonsolution.common.spring.application.async.config;

import com.olsonsolution.common.spring.application.async.props.AsyncProperties;
import com.olsonsolution.common.spring.domain.port.async.repository.ExecutorFactory;
import com.olsonsolution.common.spring.domain.port.async.repository.ThreadLocalAware;
import com.olsonsolution.common.spring.domain.port.async.repository.ThreadLocalInheritableThreadFactory;
import com.olsonsolution.common.spring.domain.service.async.ExecutorFabricatingService;
import com.olsonsolution.common.spring.domain.service.async.ThreadLocalInheritanceService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    public static final String SYSTEM_THREAD_FACTORY_BEAN = "systemThreadFactory";

    public static final String RUNTIME_THREAD_FACTORY_BEAN = "runtimeThreadFactory";

    public static final String SYSTEM_EXECUTOR_BEAN = "systemExecutor";

    public static final String RUNTIME_EXECUTOR_BEAN = "runtimeExecutor";

    @Bean
    public ExecutorFactory executorFactory() {
        return new ExecutorFabricatingService();
    }

    @Bean(SYSTEM_THREAD_FACTORY_BEAN)
    public ThreadLocalInheritableThreadFactory systemThreadFactory(AsyncProperties asyncProperties,
                                                                   List<ThreadLocalAware<?>> threadLocalAwares) {
        AsyncProperties.ExecutorProperties executorProperties = asyncProperties.getSystem();
        ThreadGroup threadGroup = new ThreadGroup(executorProperties.getGroup());
        return new ThreadLocalInheritanceService(threadGroup, threadLocalAwares);
    }

    @Bean(RUNTIME_THREAD_FACTORY_BEAN)
    public ThreadLocalInheritableThreadFactory runtimeThreadFactory(AsyncProperties asyncProperties,
                                                                    List<ThreadLocalAware<?>> threadLocalAwares) {
        AsyncProperties.ExecutorProperties executorProperties = asyncProperties.getRuntime();
        ThreadGroup threadGroup = new ThreadGroup(executorProperties.getGroup());
        return new ThreadLocalInheritanceService(threadGroup, threadLocalAwares);
    }

    @Bean(SYSTEM_EXECUTOR_BEAN)
    public Executor systemExecutor(ExecutorFactory executorFactory,
                                   @Qualifier(SYSTEM_THREAD_FACTORY_BEAN)
                                   ThreadLocalInheritableThreadFactory threadFactory,
                                   AsyncProperties asyncProperties) {
        return executorFactory.fabricate(asyncProperties.getSystem().getThreadPool(), threadFactory);
    }

    @Bean(RUNTIME_EXECUTOR_BEAN)
    public Executor runtimeExecutor(ExecutorFactory executorFactory,
                                    @Qualifier(RUNTIME_THREAD_FACTORY_BEAN)
                                    ThreadLocalInheritableThreadFactory threadFactory,
                                    AsyncProperties asyncProperties) {
        return executorFactory.fabricate(asyncProperties.getRuntime().getThreadPool(), threadFactory);
    }

}
