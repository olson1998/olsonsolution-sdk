package com.olsonsolution.common.data.domain.port.stereotype.sql;

import java.util.Map;

public interface SqlDataSource {

    SqlVendor getVendor();

    String getHost();

    Integer getPort();

    String getDatabase();

    SqlDataSourceUsers getUsers();

    Map<String, String> getProperties();

}
