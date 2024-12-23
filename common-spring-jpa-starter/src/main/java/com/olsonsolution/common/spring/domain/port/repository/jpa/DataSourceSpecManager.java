package com.olsonsolution.common.spring.domain.port.repository.jpa;

import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;

public interface DataSourceSpecManager {

    DataSourceSpec getThreadLocal();

    void setThreadLocal(DataSourceSpec dataSourceSpec);

    void clearThreadLocal();

}
