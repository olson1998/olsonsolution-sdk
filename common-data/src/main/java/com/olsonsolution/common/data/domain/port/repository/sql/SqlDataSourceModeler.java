package com.olsonsolution.common.data.domain.port.repository.sql;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.property.domain.port.stereotype.PropertySpec;

import javax.sql.DataSource;
import java.util.Collection;

public interface SqlDataSourceModeler {

    SqlVendor getSqlVendor();

    Collection<? extends PropertySpec> getPropertySpecifications();

    DataSource create(SqlDataSource dataSource, SqlUser user, SqlPermission permission);

}
