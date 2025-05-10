package com.olsonsolution.common.spring.domain.port.async.repository;

import java.util.Optional;

public interface ThreadLocalAware<T> {

    T getThreadLocal();

    Optional<T> obtainThreadLocal();

    void setThreadLocal(T value);

    void clear();

}
