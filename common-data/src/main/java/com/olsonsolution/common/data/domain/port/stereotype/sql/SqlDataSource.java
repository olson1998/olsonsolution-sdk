package com.olsonsolution.common.data.domain.port.stereotype.sql;

import java.util.Map;

public interface SqlDataSource {

    SqlVendor getVendor();

    String getHost();

    Integer getPort();

    String getDatabase();

    String getSchema();

    SqlUser getUser();

    SqlPermission getPermission();

    Map<String, String> getProperties();

}
