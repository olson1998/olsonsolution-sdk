package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecConfigurable;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

abstract class MultiVendorJpaConfigurable<D> implements DataSourceSpecConfigurable<D> {

    private final ThreadLocal<D> threadLocalDelegate = new ThreadLocal<>();

    protected final Map<Class<?>, D> delegatesRegistry = new HashMap<>();

    @Override
    public D getDelegate() {
        return Optional.ofNullable(threadLocalDelegate.get())
                .orElseThrow();
    }

    @Override
    public void setDataSourceSpec(DataSourceSpec dataSourceSpec) {
        D delegate = delegatesRegistry.computeIfAbsent(
                dataSourceSpec.getDialect(),
                c -> constructDelegate(dataSourceSpec)
        );
        threadLocalDelegate.set(delegate);
    }

    @Override
    public void unregisterDelegate(DataSourceSpec dataSourceSpec) throws Exception {
        D delegate = delegatesRegistry.get(dataSourceSpec.getDialect());
        if (delegate != null) {
            if (delegate instanceof AutoCloseable closeableDelegate) {
                closeableDelegate.close();
            }
            delegatesRegistry.remove(dataSourceSpec.getDialect());
        }
    }

    @Override
    public void clear() {
        threadLocalDelegate.remove();
    }

    protected abstract D constructDelegate(DataSourceSpec dataSourceSpec);

}
