package com.olsonsolution.common.data.domain.model.sql;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.dialect.*;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SqlVendors implements SqlVendor {

    H2(new H2Dialect(), false),
    SQL_SERVER(new SQLServerDialect(), true),
    POSTGRESQL(new PostgreSQLDialect(), true),
    DB2(new DB2Dialect(), false),
    MARIADB(new MariaDBDialect(), false),
    ;

    private final Dialect dialect;
    private final boolean supportSchemas;

    @Override
    public boolean isSameAs(SqlVendor vendor) {
        if (vendor instanceof SqlVendors vendors) {
            return this == vendors;
        } else {
            return StringUtils.equalsIgnoreCase(this.name(), vendor.name());
        }
    }
}
