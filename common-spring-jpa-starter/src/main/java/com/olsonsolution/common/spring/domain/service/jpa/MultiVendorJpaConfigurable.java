package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecConfigurable;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract class MultiVendorJpaConfigurable<D> implements DataSourceSpecConfigurable<D> {

    protected final Map<SqlVendor, D> delegatesRegistry;

    private final ThreadLocal<D> threadLocalDelegate;

    protected final DataSourceSpecManager dataSourceSpecManager;

    private final DestinationDataSourceManager destinationDataSourceManager;

    public MultiVendorJpaConfigurable(DataSourceSpecManager dataSourceSpecManager,
                                      DestinationDataSourceManager destinationDataSourceManager) {
        this.delegatesRegistry = new ConcurrentHashMap<>();
        this.threadLocalDelegate = new ThreadLocal<>();
        this.dataSourceSpecManager = dataSourceSpecManager;
        this.destinationDataSourceManager = destinationDataSourceManager;
    }

    @Override
    public D getDelegate() {
        D delegate = threadLocalDelegate.get();
        if (delegate == null) {
            DataSourceSpec dataSourceSpec = dataSourceSpecManager.getThreadLocal();
            SqlDataSource sqlDataSource = destinationDataSourceManager.obtainSqlDataSource(dataSourceSpec.getName());
            SqlVendor sqlVendor = sqlDataSource.getVendor();
            delegate = obtainDelegate(sqlVendor);
            threadLocalDelegate.set(delegate);
        }
        return delegate;
    }

    @Override
    public void unregisterDelegate(DataSourceSpec dataSourceSpec) throws Exception {
        SqlDataSource sqlDataSource = destinationDataSourceManager.obtainSqlDataSource(dataSourceSpec.getName());
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

    protected D obtainDelegate(SqlVendor sqlVendor) {
        return delegatesRegistry.computeIfAbsent(
                sqlVendor,
                c -> constructDelegate(sqlVendor)
        );
    }

    protected abstract D constructDelegate(SqlVendor sqlVendor);

}
