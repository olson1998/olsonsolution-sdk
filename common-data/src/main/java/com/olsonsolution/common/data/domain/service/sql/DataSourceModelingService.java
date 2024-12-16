package com.olsonsolution.common.data.domain.service.sql;

import com.ibm.db2.jcc.DB2Driver;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import com.olsonsolution.common.data.domain.model.exception.DataSourceModelerException;
import com.olsonsolution.common.data.domain.model.sql.SqlPermissions;
import com.olsonsolution.common.data.domain.model.sql.SqlVendors;
import com.olsonsolution.common.data.domain.port.repository.sql.DataSourceModeler;
import com.olsonsolution.common.data.domain.port.stereotype.sql.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;

import java.sql.Driver;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static com.olsonsolution.common.data.domain.model.sql.SqlPermissions.*;
import static com.olsonsolution.common.data.domain.model.sql.SqlPermissions.RWX;
import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.*;
import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.DB2;

public class DataSourceModelingService implements DataSourceModeler {

    private static final String POOL_NAME_TEMPLATE = "%s_%s_%s";

    @Override
    public DataSource createDataSource(SqlDataSource sqlDataSource, SqlPermission permission) {
        SqlVendor vendor = sqlDataSource.getVendor();
        SqlUser user = selectUser(sqlDataSource, permission);
        if(isSqlServerVendor(vendor)) {
            return DataSourceModelerUtils.createSQLServerDataSource(sqlDataSource, user, permission);
        } else if (isPostgresqlVendor(vendor)) {
            return DataSourceModelerUtils.createPostgresDataSource(sqlDataSource, user, permission);
        } else if (isDb2Vendor(vendor)) {
            return DataSourceModelerUtils.createDb2DataSource(sqlDataSource, user, permission);
        } else {
            throw new IllegalStateException("");
        }
    }

    private SqlUser selectUser(SqlDataSource sqlDataSource, SqlPermission permission) {
        Collection<? extends SqlUser> sqlUsers;
        SqlDataSourceUsers sqlDataSourceUsers = sqlDataSource.getUsers();
        if(isReadOnly(permission)) {
            sqlUsers = sqlDataSourceUsers.getReadOnly();
        } else if (isWriteOnly(permission)) {
            sqlUsers = sqlDataSourceUsers.getWriteOnly();
        } else if (isReadWrite(permission)) {
            sqlUsers = sqlDataSourceUsers.getReadWrite();
        } else if (isReadWriteExecute(permission)) {
            sqlUsers = sqlDataSourceUsers.getReadWriteExecute();
        } else {
            sqlUsers = Collections.emptyList();
        }
        return sqlUsers.stream().findAny().orElseThrow();
    }

    private boolean isReadOnly(SqlPermission permission) {
        if (permission instanceof SqlPermissions permissions) {
            return permissions == RO;
        } else {
            return StringUtils.equalsIgnoreCase(permission.name(), RO.name());
        }
    }

    private boolean isWriteOnly(SqlPermission permission) {
        if (permission instanceof SqlPermissions permissions) {
            return permissions == WO;
        } else {
            return StringUtils.equalsIgnoreCase(permission.name(), WO.name());
        }
    }

    private boolean isReadWrite(SqlPermission permission) {
        if (permission instanceof SqlPermissions permissions) {
            return permissions == RW;
        } else {
            return StringUtils.equalsIgnoreCase(permission.name(), RW.name());
        }
    }

    private boolean isReadWriteExecute(SqlPermission permission) {
        if (permission instanceof SqlPermissions permissions) {
            return permissions == RWX;
        } else {
            return StringUtils.equalsIgnoreCase(permission.name(), RWX.name());
        }
    }

    private boolean isSqlServerVendor(SqlVendor vendor) {
        if (vendor instanceof SqlVendors vendors) {
            return vendors == SQL_SERVER;
        } else {
            return StringUtils.equalsIgnoreCase(vendor.name(), SQL_SERVER.name());
        }
    }

    private boolean isPostgresqlVendor(SqlVendor vendor) {
        if (vendor instanceof SqlVendors vendors) {
            return vendors == POSTGRESQL;
        } else {
            return StringUtils.equalsIgnoreCase(vendor.name(), POSTGRESQL.name());
        }
    }

    private boolean isDb2Vendor(SqlVendor vendor) {
        if (vendor instanceof SqlVendors vendors) {
            return vendors == DB2;
        } else {
            return StringUtils.equalsIgnoreCase(vendor.name(), DB2.name());
        }
    }

}
