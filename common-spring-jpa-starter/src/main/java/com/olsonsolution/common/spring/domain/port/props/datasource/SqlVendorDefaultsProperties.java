package com.olsonsolution.common.spring.domain.port.props.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;

public interface SqlVendorDefaultsProperties {

    SqlVendor getVendorName();

    String getCatalog();

    String getSchema();

}
