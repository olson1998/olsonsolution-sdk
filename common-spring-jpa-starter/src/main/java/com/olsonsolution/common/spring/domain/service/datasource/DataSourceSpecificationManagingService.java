package com.olsonsolution.common.spring.domain.service.datasource;

import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DataSourceSpecificationManagingService implements DataSourceSpecManager {

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
