package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.EntityManagerFactoryDelegate;
import com.olsonsolution.common.spring.domain.port.repository.jpa.PlatformTransactionManagerDelegate;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

@Slf4j
public class MultiVendorPlatformTransactionManager extends MultiVendorJpaConfigurable<PlatformTransactionManager> implements PlatformTransactionManagerDelegate {

    private final EntityManagerFactoryDelegate entityManagerFactoryDelegate;

    public MultiVendorPlatformTransactionManager(DataSourceSpecManager dataSourceSpecManager,
                                                 DestinationDataSourceManager destinationDataSourceManager,
                                                 EntityManagerFactoryDelegate entityManagerFactoryDelegate) {
        super(dataSourceSpecManager, destinationDataSourceManager);
        this.entityManagerFactoryDelegate = entityManagerFactoryDelegate;
    }

    @Override
    protected PlatformTransactionManager constructDelegate(SqlVendor sqlVendor, DataSourceSpec dataSourceSpec) {
        return new JpaTransactionManager(entityManagerFactoryDelegate.getDelegate());
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
