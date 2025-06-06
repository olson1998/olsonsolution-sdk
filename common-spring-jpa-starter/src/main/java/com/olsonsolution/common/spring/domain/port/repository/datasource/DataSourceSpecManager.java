package com.olsonsolution.common.spring.domain.port.repository.datasource;

import com.olsonsolution.common.spring.domain.port.async.repository.ThreadLocalAware;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;

public interface DataSourceSpecManager extends ThreadLocalAware<DataSourceSpec> {
}
