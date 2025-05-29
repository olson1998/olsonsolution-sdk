package com.olsonsolution.common.spring.domain.port.repository.jpa;

import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpecification;

public interface DataSourceSpecConfigurable<D> {

    D getDelegate();

    void unregisterDelegate(DataSourceSpecification dataSourceSpecification) throws Exception;

}
