package com.olsonsolution.common.data.domain.model.sql;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;

public enum SqlPermissions implements SqlPermission {

    RO,
    WO,
    RW,
    RWX

}
