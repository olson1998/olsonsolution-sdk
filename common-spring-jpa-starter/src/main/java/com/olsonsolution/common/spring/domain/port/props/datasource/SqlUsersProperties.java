package com.olsonsolution.common.spring.domain.port.props.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSourceUsers;

public interface SqlUsersProperties extends SqlDataSourceUsers {

    String getSchema();

}
