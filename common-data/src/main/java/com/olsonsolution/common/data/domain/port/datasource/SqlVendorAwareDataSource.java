package com.olsonsolution.common.data.domain.port.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;

import javax.sql.DataSource;

public interface SqlVendorAwareDataSource extends DataSource {

    SqlVendor getVendor();

}
