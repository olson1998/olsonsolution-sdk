package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaEnvironmentConfigurable;
import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

abstract class MultiVendorJpaConfigurable<D> implements JpaEnvironmentConfigurable<D> {

    private final ThreadLocal<D> threadLocalDelegate = new ThreadLocal<>();

    protected final Map<Class<?>, D> delegatesRegistry = new HashMap<>();

    @Override
    public D getDelegate() {
        return Optional.ofNullable(threadLocalDelegate.get())
                .orElseThrow();
    }

    @Override
    public void setEnvironment(JpaEnvironment jpaEnvironment) {
        D delegate = delegatesRegistry.computeIfAbsent(
                jpaEnvironment.getDialect(),
                c -> constructDelegate(jpaEnvironment)
        );
        threadLocalDelegate.set(delegate);
    }

    @Override
    public void unregisterDelegate(JpaEnvironment jpaEnvironment) throws Exception {
        D delegate = delegatesRegistry.get(jpaEnvironment.getDialect());
        if(delegate != null) {
            if(delegate instanceof AutoCloseable closeableDelegate) {
                closeableDelegate.close();
            }
            delegatesRegistry.remove(jpaEnvironment.getDialect());
        }
    }

    @Override
    public void clear() {
        threadLocalDelegate.remove();
    }

    protected abstract D constructDelegate(JpaEnvironment jpaEnvironment);

}
