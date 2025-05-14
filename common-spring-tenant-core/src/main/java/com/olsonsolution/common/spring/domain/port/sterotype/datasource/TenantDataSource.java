package com.olsonsolution.common.spring.domain.port.sterotype.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.spring.domain.port.sterotype.tenant.Tenant;

public interface TenantDataSource {

    Tenant getTenant();

    SqlDataSource getDataSource();

}
