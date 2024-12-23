package com.olsonsolution.common.data.domain.model.sql;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import org.apache.commons.lang3.StringUtils;

public enum SqlVendors implements SqlVendor {

    H2,
    SQL_SERVER,
    POSTGRESQL,
    DB2,
    MARIADB;

    @Override
    public boolean isSameAs(SqlVendor vendor) {
        if(vendor instanceof SqlVendors vendors) {
            return this == vendors;
        } else {
            return StringUtils.equalsIgnoreCase(this.name(), vendor.name());
        }
    }
}
