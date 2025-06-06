package com.olsonsolution.common.data.domain.port.stereotype.sql;

public interface SqlDataSourceUsers {

    SqlUser getReadOnly();

    SqlUser getWriteOnly();

    SqlUser getReadWrite();

    SqlUser getReadWriteExecute();

}
