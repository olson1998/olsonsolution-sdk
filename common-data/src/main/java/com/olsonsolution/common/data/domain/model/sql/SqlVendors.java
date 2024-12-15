package com.olsonsolution.common.data.domain.model.sql;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;

public enum SqlVendors implements SqlVendor {

    SQL_SERVER,
    POSTGRESQL,
    DB2,
    SQLLITE,
    MARIADB

}
