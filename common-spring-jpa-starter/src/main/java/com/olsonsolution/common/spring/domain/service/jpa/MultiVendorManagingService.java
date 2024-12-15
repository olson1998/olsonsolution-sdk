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
    public void setCurrent(DataSourceSpec dataSourceSpec) {
        dataSourceSpecThreadLocal.set(dataSourceSpec);
        log.debug("Configuring thread data source specification: {}", dataSourceSpec);
    }

    @Override
    public void clear() {
        dataSourceSpecThreadLocal.remove();
    }

}
