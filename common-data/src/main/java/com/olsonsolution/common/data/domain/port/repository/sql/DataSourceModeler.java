package com.olsonsolution.common.data.domain.port.repository.sql;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;

import javax.sql.DataSource;

public interface DataSourceModeler {

    SqlVendor getSqlVendor();

    DataSource create(SqlDataSource dataSource, SqlUser user, SqlPermission permission);

}
