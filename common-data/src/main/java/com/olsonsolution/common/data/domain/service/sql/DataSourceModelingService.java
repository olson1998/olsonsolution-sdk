package com.olsonsolution.common.data.domain.service.sql;

import com.olsonsolution.common.data.domain.port.repository.sql.DataSourceModeler;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;

import javax.sql.DataSource;

public class DataSourceModelingService implements DataSourceModeler {

    @Override
    public DataSource createDataSource(SqlDataSource sqlDataSource, SqlPermission permission) {
        SqlUser sqlUser = DataSourceModelerUtils.selectUserByPermission(sqlDataSource, permission);
        return DataSourceModelerUtils.createDataSource(sqlDataSource, sqlUser, permission);
    }

}
