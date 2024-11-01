package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingEntityManagerFactory;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingPlatformTransactionManager;
import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

@Slf4j
@RequiredArgsConstructor
public class MultiVendorPlatformTransactionManager extends MultiVendorJpaConfigurable<PlatformTransactionManager> implements RoutingPlatformTransactionManager {

    private final RoutingEntityManagerFactory routingEntityManagerFactory;

    @Override
    protected PlatformTransactionManager constructDelegate(JpaEnvironment jpaEnvironment) {
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
