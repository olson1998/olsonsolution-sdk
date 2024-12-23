package com.olsonsolution.common.spring.domain.port.repository.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;

public interface DestinationDataSourceManager {

    SqlDataSource obtainSqlDataSource(DataSourceSpec dataSourceSpec);

}
