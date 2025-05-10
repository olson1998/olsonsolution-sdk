package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.spring.domain.model.exception.datasource.DataSourceException;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecConfigurable;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract class MultiVendorJpaConfigurable<D> implements DataSourceSpecConfigurable<D>, AutoCloseable {

    protected final Map<SqlVendor, D> delegatesRegistry;

    protected final DataSourceSpecManager dataSourceSpecManager;

    private final SqlDataSourceProvider sqlDataSourceProvider;

    public MultiVendorJpaConfigurable(DataSourceSpecManager dataSourceSpecManager,
                                      SqlDataSourceProvider sqlDataSourceProvider) {
        this.dataSourceSpecManager = dataSourceSpecManager;
        this.sqlDataSourceProvider = sqlDataSourceProvider;
        this.delegatesRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public D getDelegate() {
        DataSourceSpec dataSourceSpec = dataSourceSpecManager.getThreadLocal();
        if (dataSourceSpec == null) {
            throw new DataSourceException("No DataSourceSpec configured for current thread");
        }
        SqlDataSource sqlDataSource = sqlDataSourceProvider.findDestination(dataSourceSpec.getName())
                .orElseThrow(() -> new DataSourceException("Destination data source not found for name: '%s'"
                        .formatted(dataSourceSpec.getName())));
        SqlVendor vendor = sqlDataSource.getVendor();
        return obtainDelegate(vendor);
    }

    @Override
    public void unregisterDelegate(DataSourceSpec dataSourceSpec) throws Exception {
        SqlDataSource sqlDataSource = sqlDataSourceProvider.findDestination(dataSourceSpec.getName())
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
    public void close() throws Exception {
        for (Map.Entry<SqlVendor, D> registeredDelegate : delegatesRegistry.entrySet()) {
            if (registeredDelegate.getValue() instanceof AutoCloseable closeableDelegate) {
                closeableDelegate.close();
            }
        }
    }

    protected D obtainDelegate(SqlVendor sqlVendor) {
        return delegatesRegistry.computeIfAbsent(
                sqlVendor,
                this::constructDelegate
        );
    }

    protected abstract D constructDelegate(SqlVendor sqlVendor);

}
