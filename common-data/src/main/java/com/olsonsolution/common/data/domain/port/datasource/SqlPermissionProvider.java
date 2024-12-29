package com.olsonsolution.common.data.domain.port.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;

public interface SqlPermissionProvider {

    SqlPermission getThreadLocalPermission();

}
