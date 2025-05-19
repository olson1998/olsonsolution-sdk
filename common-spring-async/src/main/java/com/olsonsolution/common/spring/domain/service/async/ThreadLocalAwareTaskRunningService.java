package com.olsonsolution.common.spring.domain.service.async;

import com.olsonsolution.common.spring.domain.port.async.repository.ThreadLocalAware;
import com.olsonsolution.common.spring.domain.port.async.repository.ThreadLocalAwareTaskRunner;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ThreadLocalAwareTaskRunningService implements ThreadLocalAwareTaskRunner {

    private final List<ThreadLocalAware<?>> threadLocalAwares;

    @Override
    public Runnable runWithContext(String taskName, Runnable runnable) {
        Map<ThreadLocalAware<Object>, Optional<?>> inheritableThreadLocals = mapInheritableThreadLocals();
        return () -> {
            try {
                restoreInheritableThreadLocals(inheritableThreadLocals);
                runnable.run();
            } finally {
                clearInheritableThreadLocals(inheritableThreadLocals);
            }
        };
    }

    private Map<ThreadLocalAware<Object>, Optional<?>> mapInheritableThreadLocals() {
        return threadLocalAwares.stream()
                .map(this::obtainInheritable)
                .collect(Collectors.toUnmodifiableMap(KeyValue::getKey, KeyValue::getValue));
    }

    private void restoreInheritableThreadLocals(Map<ThreadLocalAware<Object>, Optional<?>> threadLocals) {
        threadLocals.forEach((aware, value) -> value.ifPresent(v -> {
            aware.setThreadLocal(v);
        }));
    }

    private void clearInheritableThreadLocals(Map<ThreadLocalAware<Object>, Optional<?>> threadLocals) {
        threadLocals.keySet().forEach(ThreadLocalAware::clear);
    }

    private KeyValue<ThreadLocalAware<Object>, Optional<?>> obtainInheritable(ThreadLocalAware<?> aware) {
        return new DefaultKeyValue<>((ThreadLocalAware<Object>) aware, aware.obtainThreadLocal());
    }

}
