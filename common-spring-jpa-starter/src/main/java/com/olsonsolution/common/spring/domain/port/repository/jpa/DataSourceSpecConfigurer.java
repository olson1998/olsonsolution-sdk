package com.olsonsolution.common.spring.domain.port.repository.jpa;

import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;

public interface DataSourceSpecConfigurer {

    void configure(DataSourceSpec dataSourceSpec);

    void clear();

}
