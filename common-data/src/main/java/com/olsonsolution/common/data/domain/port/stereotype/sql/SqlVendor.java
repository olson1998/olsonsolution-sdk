package com.olsonsolution.common.data.domain.port.stereotype.sql;

public interface SqlVendor {

    String name();

    boolean isSameAs(SqlVendor vendor);

}
