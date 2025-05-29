package com.olsonsolution.common.spring.domain.port.repository.jpa;

import com.olsonsolution.common.data.domain.port.datasource.SqlPermissionProvider;
import com.olsonsolution.common.spring.domain.port.async.repository.ThreadLocalAware;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpecification;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public interface DataSourceSpecManager extends ThreadLocalAware<DataSourceSpecification>,
        SqlPermissionProvider,
        CurrentTenantIdentifierResolver<DataSourceSpecification> {

}
