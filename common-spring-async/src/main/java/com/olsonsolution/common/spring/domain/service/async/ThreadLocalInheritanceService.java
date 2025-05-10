package com.olsonsolution.common.spring.domain.service.async;

import com.olsonsolution.common.spring.domain.port.async.repository.ThreadLocalInheritableThreadFactory;
import com.olsonsolution.common.spring.domain.port.async.repository.ThreadLocalAware;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ThreadLocalInheritanceService implements ThreadLocalInheritableThreadFactory {

    private final ThreadGroup threadGroup;

    private final List<ThreadLocalAware<?>> threadLocalAwares;

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = Thread.currentThread();
        String originalName = thread.getName();
        Map<ThreadLocalAware<Object>, Optional<Object>> threadLocals = collectThreadLocals();
        return new Thread(threadGroup, () -> decoratedRunnable(r, originalName, threadLocals));
    }

    private Map<ThreadLocalAware<Object>, Optional<Object>> collectThreadLocals() {
        return threadLocalAwares.stream()
                .map(this::obtainThreadLocal)
                .collect(Collectors.toUnmodifiableMap(KeyValue::getKey, KeyValue::getValue));
    }

    private void transferThreadLocal(ThreadLocalAware<Object> aware, Optional<?> value) {
        value.ifPresent(aware::setThreadLocal);
    }

    private KeyValue<ThreadLocalAware<Object>, Optional<Object>> obtainThreadLocal(ThreadLocalAware<?> aware) {
        return new DefaultKeyValue<>((ThreadLocalAware<Object>) aware, (Optional<Object>) aware.obtainThreadLocal());
    }

    private void decoratedRunnable(Runnable runnable,
                                   String originalName,
                                   Map<ThreadLocalAware<Object>, Optional<Object>> threadLocals) {
        Thread thread = Thread.currentThread();
        String decoratedName = threadGroup.getName() + '-' + thread.getId();
        try {
            threadLocals.forEach(this::transferThreadLocal);
            thread.setName(decoratedName);
            runnable.run();
        } finally {
            thread.setName(originalName);
            threadLocals.keySet().forEach(ThreadLocalAware::clear);
        }
    }

}
