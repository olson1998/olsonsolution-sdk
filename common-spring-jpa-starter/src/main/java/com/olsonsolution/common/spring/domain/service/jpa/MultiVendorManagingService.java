package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.EntityManagerFactoryDelegate;
import com.olsonsolution.common.spring.domain.port.repository.jpa.PlatformTransactionManagerDelegate;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class MultiVendorManagingService implements DataSourceSpecManager {

    private final ThreadLocal<DataSourceSpec> dataSourceSpecThreadLocal = new ThreadLocal<>();

    private final List<EntityManagerFactoryDelegate> entityManagerFactoryDelegates = new ArrayList<>();

    private final List<PlatformTransactionManagerDelegate> platformTransactionManagerDelegates = new ArrayList<>();

    @Override
    public DataSourceSpec getThreadLocal() {
        return dataSourceSpecThreadLocal.get();
    }

    @Override
    public void configureThreadLocal(DataSourceSpec dataSourceSpec) {
        dataSourceSpecThreadLocal.set(dataSourceSpec);
        entityManagerFactoryDelegates.forEach(emf -> emf.setThreadLocal(dataSourceSpec));
        platformTransactionManagerDelegates
                .forEach(tpm -> tpm.setThreadLocal(dataSourceSpec));
    }

    @Override
    public void register(EntityManagerFactoryDelegate entityManagerFactory) {
        entityManagerFactoryDelegates.add(entityManagerFactory);
    }

    @Override
    public void register(PlatformTransactionManagerDelegate platformTransactionManager) {
        platformTransactionManagerDelegates.add(platformTransactionManager);
    }

    @Override
    public void clearThreadLocalConfig() {
        platformTransactionManagerDelegates.forEach(PlatformTransactionManagerDelegate::clear);
        entityManagerFactoryDelegates.forEach(EntityManagerFactoryDelegate::clear);
        dataSourceSpecThreadLocal.remove();
    }

}
