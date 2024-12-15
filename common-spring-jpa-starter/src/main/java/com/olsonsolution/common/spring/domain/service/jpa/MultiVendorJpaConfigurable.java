package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecConfigurable;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

abstract class MultiVendorJpaConfigurable<D> implements DataSourceSpecConfigurable<D> {

    protected final Map<SqlVendor, D> delegatesRegistry;

    private final ThreadLocal<D> threadLocalDelegate;

    private final DestinationDataSourceProvider destinationDataSourceProvider;

    public MultiVendorJpaConfigurable(DestinationDataSourceProvider destinationDataSourceProvider) {
        this.delegatesRegistry = new ConcurrentHashMap<>();
        this.threadLocalDelegate = new ThreadLocal<>();
        this.destinationDataSourceProvider = destinationDataSourceProvider;
    }

    @Override
    public D getDelegate() {
        return Optional.ofNullable(threadLocalDelegate.get())
                .orElseThrow();
    }

    @Override
    public void setDataSourceSpec(DataSourceSpec dataSourceSpec) {
        SqlDataSource sqlDataSource = destinationDataSourceProvider.findDestination(dataSourceSpec.getName())
                .orElseThrow();
        SqlVendor sqlVendor = sqlDataSource.getVendor();
        setSqlVendorSpec(sqlVendor, dataSourceSpec);
    }

    @Override
    public void unregisterDelegate(DataSourceSpec dataSourceSpec) throws Exception {
        SqlDataSource sqlDataSource = destinationDataSourceProvider.findDestination(dataSourceSpec.getName())
                .orElseThrow();
        SqlVendor vendor = sqlDataSource.getVendor();
        D delegate = delegatesRegistry.get(vendor);
        if (delegate != null) {
            if (delegate instanceof AutoCloseable closeableDelegate) {
                closeableDelegate.close();
            }
            delegatesRegistry.remove(vendor);
        }
    }

    @Override
    public void clear() {
        threadLocalDelegate.remove();
    }

    protected void setSqlVendorSpec(SqlVendor sqlVendor, DataSourceSpec dataSourceSpec) {
        D delegate = delegatesRegistry.computeIfAbsent(
                sqlVendor,
                c -> constructDelegate(sqlVendor, dataSourceSpec)
        );
        threadLocalDelegate.set(delegate);
    }

    protected abstract D constructDelegate(SqlVendor sqlVendor, DataSourceSpec dataSourceSpec);

}
