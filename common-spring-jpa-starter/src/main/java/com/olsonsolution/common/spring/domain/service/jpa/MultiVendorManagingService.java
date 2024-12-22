package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MultiVendorManagingService implements DataSourceSpecManager {

    private boolean initialized;

    private boolean initializing;

    @Setter
    private DataSourceSpec initDataSourceSpec;

    private final ThreadLocal<DataSourceSpec> dataSourceSpecThreadLocal = new ThreadLocal<>();

    @Override
    public DataSourceSpec getThreadLocal() {
        return dataSourceSpecThreadLocal.get();
    }

    @Override
    public void setCurrent(DataSourceSpec dataSourceSpec) {
        if (initialized) {
            log.debug("Configuring thread data source specification: {}", dataSourceSpec);
        } else if (!initializing) {
            initializing = true;
            initDataSourceSpec = dataSourceSpec;
            log.info("Data source manager init spec={} is initializing...", initDataSourceSpec);
        } else if (initDataSourceSpec != null &&
                dataSourceSpecThreadLocal.get() != null &&
                dataSourceSpecThreadLocal.get() == initDataSourceSpec) {
            initializing = false;
            initialized = true;
            initDataSourceSpec = null;
            log.info("Data source manager initialized");
        }
        dataSourceSpecThreadLocal.set(dataSourceSpec);
    }

    @Override
    public void clear() {
        dataSourceSpecThreadLocal.remove();
    }

}
