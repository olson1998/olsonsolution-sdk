package com.olsonsolution.common.spring.domain.port.async.repository;

public interface ThreadLocalAwareTaskRunner {

    Runnable runWithContext(String taskName, Runnable runnable);

}
