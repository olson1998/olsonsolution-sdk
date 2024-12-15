package com.olsonsolution.common.data.domain.port.stereotype.sql;

import java.util.Properties;

public interface SqlDataSource {

    SqlVendor getVendor();

    String getHost();

    Integer getPort();

    String getDatabase();

    SqlDataSourceUsers getUsers();

    Properties getProperties();

}
