package com.olsonsolution.common.data.domain.port.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;

import javax.sql.DataSource;

public interface PermissionManagingDataSource extends DataSource, AutoCloseable {

    boolean isClosed();

    SqlDataSource getSqlDataSource();

    DataSource getByPermission(SqlPermission permission);

}
