package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecConfigurable;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract class MultiVendorJpaConfigurable<D> implements DataSourceSpecConfigurable<D>, AutoCloseable {

    protected final Map<SqlVendor, D> delegatesRegistry;

    private final DataSourceSpecManager dataSourceSpecManager;

    private final DestinationDataSourceManager destinationDataSourceManager;

    public MultiVendorJpaConfigurable(DataSourceSpecManager dataSourceSpecManager,
                                      DestinationDataSourceManager destinationDataSourceManager) {
        this.dataSourceSpecManager = dataSourceSpecManager;
        this.delegatesRegistry = new ConcurrentHashMap<>();
        this.destinationDataSourceManager = destinationDataSourceManager;
    }

    @Override
    public D getDelegate() {
        DataSourceSpec dataSourceSpec = dataSourceSpecManager.getThreadLocal();
        if (dataSourceSpec == null) {
            throw new IllegalStateException("No DataSourceSpec configured for current thread");
        }
        SqlDataSource sqlDataSource = destinationDataSourceManager.obtainSqlDataSource(dataSourceSpec);
        SqlVendor vendor = sqlDataSource.getVendor();
        return obtainDelegate(vendor);
    }

    @Override
    public void unregisterDelegate(DataSourceSpec dataSourceSpec) throws Exception {
        SqlDataSource sqlDataSource = destinationDataSourceManager.obtainSqlDataSource(dataSourceSpec);
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
        for(Map.Entry<SqlVendor, D> registedDelegate : delegatesRegistry.entrySet()) {
            if(registedDelegate.getValue() instanceof AutoCloseable closeableDelegate) {
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
