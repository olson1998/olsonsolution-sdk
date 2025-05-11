package com.olsonsolution.common.spring.domain.service.async;

import com.olsonsolution.common.spring.domain.port.async.repository.ThreadLocalAwareTaskRunner;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ThreadFactory;

@RequiredArgsConstructor
public class ThreadLocalAwareTaskThreadFactory implements ThreadFactory {

    private final String name;

    private final ThreadLocalAwareTaskRunner threadLocalAwareTaskRunner;

    @Override
    public Thread newThread(@NonNull Runnable r) {
        Runnable task = threadLocalAwareTaskRunner.runWithContext(name, r);
        return new Thread(task);
    }
}
