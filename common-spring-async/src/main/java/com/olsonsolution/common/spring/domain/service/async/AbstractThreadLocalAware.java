package com.olsonsolution.common.spring.domain.service.async;

import com.olsonsolution.common.spring.domain.port.async.repository.ThreadLocalAware;

import java.util.Optional;

public abstract class AbstractThreadLocalAware<T> implements ThreadLocalAware<T> {

    private final ThreadLocal<T> threadLocal = new ThreadLocal<>();

    @Override
    public T getThreadLocal() {
        return threadLocal.get();
    }

    @Override
    public Optional<T> obtainThreadLocal() {
        return Optional.ofNullable(threadLocal.get());
    }

    @Override
    public void setThreadLocal(T value) {
        threadLocal.set(value);
    }

    @Override
    public void clear() {
        threadLocal.remove();
    }

}
