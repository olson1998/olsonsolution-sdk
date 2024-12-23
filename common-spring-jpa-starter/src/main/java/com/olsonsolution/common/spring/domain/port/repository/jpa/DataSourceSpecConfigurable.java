package com.olsonsolution.common.spring.domain.port.repository.jpa;

import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;

public interface DataSourceSpecConfigurable<D> {

    D getDelegate();

    void setThreadLocal(DataSourceSpec dataSourceSpec);

    void unregisterDelegate(DataSourceSpec dataSourceSpec) throws Exception;

    void clear();

}
