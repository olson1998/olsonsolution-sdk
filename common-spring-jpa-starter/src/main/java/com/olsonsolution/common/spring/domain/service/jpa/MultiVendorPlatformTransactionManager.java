package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.EntityManagerFactoryDelegate;
import com.olsonsolution.common.spring.domain.port.repository.jpa.PlatformTransactionManagerDelegate;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

@Slf4j
public class MultiVendorPlatformTransactionManager extends MultiVendorJpaConfigurable<PlatformTransactionManager>
        implements PlatformTransactionManagerDelegate {

    private final EntityManagerFactoryDelegate entityManagerFactoryDelegate;

    public MultiVendorPlatformTransactionManager(DataSourceSpecManager dataSourceSpecManager,
                                                 SqlDataSourceProvider sqlDataSourceProvider,
                                                 EntityManagerFactoryDelegate entityManagerFactoryDelegate) {
        super(dataSourceSpecManager, sqlDataSourceProvider);
        this.entityManagerFactoryDelegate = entityManagerFactoryDelegate;
    }

    @Override
    protected PlatformTransactionManager constructDelegate(SqlVendor sqlVendor) {
        return new JpaTransactionManager(entityManagerFactoryDelegate);
    }

    @NonNull
    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        return getDelegate().getTransaction(definition);
    }

    @Override
    public void commit(@NonNull TransactionStatus status) throws TransactionException {
        getDelegate().commit(status);
    }

    @Override
    public void rollback(@NonNull TransactionStatus status) throws TransactionException {
        getDelegate().rollback(status);
    }
}
