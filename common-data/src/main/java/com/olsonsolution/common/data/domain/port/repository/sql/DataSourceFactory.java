package com.olsonsolution.common.data.domain.port.repository.sql;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;

import javax.sql.DataSource;

public interface DataSourceFactory {

    DataSource fabricate(SqlDataSource sqlDataSource, SqlPermission permission);

}
