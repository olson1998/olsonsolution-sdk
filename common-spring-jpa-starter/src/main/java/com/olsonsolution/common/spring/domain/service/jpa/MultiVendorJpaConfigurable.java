package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.spring.domain.model.datasource.DomainJpaSpecDataSource;
import com.olsonsolution.common.spring.domain.model.exception.datasource.DataSourceException;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecConfigurable;
import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaSpecDataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.JpaDataSourceSpec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract class MultiVendorJpaConfigurable<D> implements DataSourceSpecConfigurable<D>, AutoCloseable {

    protected final String jpaSpec;

    protected final Map<SqlVendor, D> delegatesRegistry;

    protected final DataSourceSpecManager dataSourceSpecManager;

    protected final JpaSpecDataSourceSpecManager jpaSpecDataSourceSpecManager;

    private final SqlDataSourceProvider sqlDataSourceProvider;

    public MultiVendorJpaConfigurable(String jpaSpec,
                                      DataSourceSpecManager dataSourceSpecManager,
                                      JpaSpecDataSourceSpecManager jpaSpecDataSourceSpecManager,
                                      SqlDataSourceProvider sqlDataSourceProvider) {
        this.jpaSpec = jpaSpec;
        this.dataSourceSpecManager = dataSourceSpecManager;
        this.jpaSpecDataSourceSpecManager = jpaSpecDataSourceSpecManager;
        this.sqlDataSourceProvider = sqlDataSourceProvider;
        this.delegatesRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public D getDelegate() {
        DataSourceSpec spec = dataSourceSpecManager.getThreadLocal();
        if (spec == null) {
            throw new DataSourceException("No DataSourceSpec configured for current thread");
        }
        JpaDataSourceSpec jpaDataSourceSpec =
                new DomainJpaSpecDataSource(jpaSpec, spec.getDataSourceName(), spec.getPermission());
        jpaSpecDataSourceSpecManager.setThreadLocal(jpaDataSourceSpec);
        SqlDataSource sqlDataSource = sqlDataSourceProvider.findDestination(jpaDataSourceSpec)
                .orElseThrow(() -> new DataSourceException(
                        "Destination data source not found for: '%s'".formatted(jpaDataSourceSpec)
                ));
        SqlVendor vendor = sqlDataSource.getVendor();
        return obtainDelegate(vendor);
    }

    @Override
    public void unregisterDelegate(JpaDataSourceSpec jpaDataSourceSpec) throws Exception {
        SqlDataSource sqlDataSource = sqlDataSourceProvider.findDestination(jpaDataSourceSpec)
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
        return delegatesRegistry.computeIfAbsent(sqlVendor, this::constructDelegate);
    }

    protected abstract D constructDelegate(SqlVendor sqlVendor);

}
