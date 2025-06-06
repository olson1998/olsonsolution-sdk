package com.olsonsolution.common.spring.domain.port.props.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;

import java.util.List;
import java.util.Properties;

public interface SqlDataSourceProperties {

    String getName();

    SqlVendor getVendor();

    String getHost();

    String getDatabase();

    Integer getPort();

    List<? extends SqlUsersProperties> getUser();

    Properties getProperties();

}
