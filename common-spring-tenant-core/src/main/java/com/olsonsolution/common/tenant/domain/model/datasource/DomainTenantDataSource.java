package com.olsonsolution.common.tenant.domain.model.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.tenant.domain.port.sterotype.tenant.Tenant;
import com.olsonsolution.common.tenant.domain.port.sterotype.datasource.TenantDataSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainTenantDataSource implements TenantDataSource {

    private Tenant tenant;

    private SqlDataSource dataSource;

}
