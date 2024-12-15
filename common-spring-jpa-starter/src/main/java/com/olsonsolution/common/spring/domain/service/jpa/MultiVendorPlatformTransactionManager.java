package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingEntityManagerFactory;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingPlatformTransactionManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

@Slf4j
public class MultiVendorPlatformTransactionManager extends MultiVendorJpaConfigurable<PlatformTransactionManager> implements RoutingPlatformTransactionManager {

    private final RoutingEntityManagerFactory routingEntityManagerFactory;

    public MultiVendorPlatformTransactionManager(DataSourceSpecManager dataSourceSpecManager,
                                                 DestinationDataSourceManager destinationDataSourceManager,
                                                 RoutingEntityManagerFactory routingEntityManagerFactory) {
        super(dataSourceSpecManager, destinationDataSourceManager);
        this.routingEntityManagerFactory = routingEntityManagerFactory;
    }

    @Override
    protected PlatformTransactionManager constructDelegate(SqlVendor sqlVendor, DataSourceSpec dataSourceSpec) {
        return new JpaTransactionManager(routingEntityManagerFactory.getDelegate());
    }

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        return getDelegate().getTransaction(definition);
    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {
        getDelegate().commit(status);
    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
        getDelegate().rollback(status);
    }
}
