package com.olsonsolution.common.spring.domain.port.repository.jpa;

import com.olsonsolution.common.data.domain.port.datasource.SqlPermissionProvider;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public interface DataSourceSpecManager extends SqlPermissionProvider, CurrentTenantIdentifierResolver<DataSourceSpec> {

    DataSourceSpec getThreadLocal();

    void setThreadLocal(DataSourceSpec dataSourceSpec);

    void clearThreadLocal();

}
