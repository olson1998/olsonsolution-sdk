package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MultiVendorManagingService implements DataSourceSpecManager {

    private final ThreadLocal<DataSourceSpec> dataSourceSpecThreadLocal = new ThreadLocal<>();

    @Override
    public DataSourceSpec getThreadLocal() {
        return dataSourceSpecThreadLocal.get();
    }

    @Override
    public void setThreadLocal(DataSourceSpec dataSourceSpec) {
        dataSourceSpecThreadLocal.set(dataSourceSpec);
    }

    @Override
    public void clearThreadLocal() {
        dataSourceSpecThreadLocal.remove();
    }

}
