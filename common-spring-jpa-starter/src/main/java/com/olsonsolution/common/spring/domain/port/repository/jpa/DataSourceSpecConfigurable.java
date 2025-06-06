package com.olsonsolution.common.spring.domain.port.repository.jpa;

import com.olsonsolution.common.spring.domain.port.stereotype.datasource.JpaDataSourceSpec;

public interface DataSourceSpecConfigurable<D> {

    D getDelegate();

    void unregisterDelegate(JpaDataSourceSpec jpaDataSourceSpec) throws Exception;

}
