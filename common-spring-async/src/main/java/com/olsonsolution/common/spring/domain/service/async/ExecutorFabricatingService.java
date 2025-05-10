package com.olsonsolution.common.spring.domain.service.async;

import com.olsonsolution.common.spring.domain.port.async.props.ThreadPoolProperties;
import com.olsonsolution.common.spring.domain.port.async.repository.ExecutorFactory;
import com.olsonsolution.common.spring.domain.port.async.repository.ThreadLocalInheritableThreadFactory;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

@RequiredArgsConstructor
public class ExecutorFabricatingService implements ExecutorFactory {

    @Override
    public Executor fabricate(ThreadPoolProperties properties,
                              ThreadLocalInheritableThreadFactory threadFactory) {
        Duration keepAlive = properties.getKeepAlive();
        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(properties.getQueueCapacity());
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                properties.getSize(),
                properties.getMaxPoolSize(),
                keepAlive.toNanos(),
                NANOSECONDS,
                queue
        );
        executor.setThreadFactory(threadFactory);
        return executor;
    }
}
