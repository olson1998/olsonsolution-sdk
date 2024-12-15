package com.olsonsolution.common.data.domain.port.stereotype.sql;

import java.util.Collection;

public interface SqlDataSourceUsers {

    Collection<? extends SqlUser> getReadOnly();

    Collection<? extends SqlUser> getWriteOnly();

    Collection<? extends SqlUser> getReadWrite();

    Collection<? extends SqlUser> getReadWriteExecute();

}
