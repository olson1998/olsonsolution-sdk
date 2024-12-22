package com.olsonsolution.common.data.domain.model.sql;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import org.apache.commons.lang3.StringUtils;

public enum SqlPermissions implements SqlPermission {

    RO,
    WO,
    RW,
    RWX;

    @Override
    public boolean isSameAs(SqlPermission permission) {
        if(permission instanceof SqlPermissions permissions) {
            return this == permissions;
        } else {
            return StringUtils.equalsIgnoreCase(permission.name(), name());
        }
    }
}
