package com.olsonsolution.common.spring.domain.port.stereotype.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;

public interface DataSourceSpec {

    String getDataSourceName();

    SqlPermission getPermission();

}
