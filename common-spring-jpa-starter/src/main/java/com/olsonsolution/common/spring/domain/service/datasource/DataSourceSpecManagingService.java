package com.olsonsolution.common.spring.domain.service.datasource;

import com.olsonsolution.common.spring.domain.port.repository.datasource.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import com.olsonsolution.common.spring.domain.service.async.AbstractThreadLocalAware;

public class DataSourceSpecManagingService extends AbstractThreadLocalAware<DataSourceSpec> implements DataSourceSpecManager {
}
