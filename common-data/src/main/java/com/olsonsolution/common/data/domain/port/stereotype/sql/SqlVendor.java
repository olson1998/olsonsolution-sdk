package com.olsonsolution.common.data.domain.port.stereotype.sql;

import org.hibernate.dialect.Dialect;

public interface SqlVendor {

    Dialect getDialect();

    String name();

    boolean isSupportSchemas();

    boolean isSameAs(SqlVendor vendor);

}
