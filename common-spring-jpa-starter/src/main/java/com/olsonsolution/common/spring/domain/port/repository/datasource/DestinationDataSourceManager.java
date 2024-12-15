package com.olsonsolution.common.spring.domain.port.repository.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;

public interface DestinationDataSourceManager {

    SqlDataSource obtainSqlDataSource(String dataSourceName);

}
