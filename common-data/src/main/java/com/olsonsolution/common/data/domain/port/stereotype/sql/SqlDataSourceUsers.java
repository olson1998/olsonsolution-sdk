package com.olsonsolution.common.data.domain.port.stereotype.sql;

import java.util.List;

public interface SqlDataSourceUsers {

    List<? extends SqlUser> getReadOnly();

    List<? extends SqlUser> getWriteOnly();

    List<? extends SqlUser> getReadWrite();

    List<? extends SqlUser> getReadWriteExecute();

}
