package com.olsonsolution.common.tenant.domain.port.sterotype.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.tenant.domain.port.sterotype.tenant.Tenant;

public interface TenantDataSource {

    Tenant getTenant();

    SqlDataSource getDataSource();

}
