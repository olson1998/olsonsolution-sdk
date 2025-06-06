package com.olsonsolution.common.spring.domain.model.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.JpaDataSourceSpec;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainJpaSpecDataSource implements JpaDataSourceSpec {

    private String jpaSpec;

    private String dataSourceName;

    private SqlPermission permission;

}
