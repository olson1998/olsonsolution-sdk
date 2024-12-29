package com.olsonsolution.common.data.domain.service.sql;

import com.olsonsolution.common.data.domain.port.repository.sql.SqlDataSourceFactory;
import com.olsonsolution.common.data.domain.port.repository.sql.SqlDataSourceModeler;
import com.olsonsolution.common.data.domain.port.stereotype.sql.*;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static com.olsonsolution.common.data.domain.model.sql.SqlPermissions.*;

@RequiredArgsConstructor
public class DataSourceFabricatingService implements SqlDataSourceFactory {

    private final List<SqlDataSourceModeler> sqlDataSourceModelers;

    @Override
    public DataSource fabricate(SqlDataSource sqlDataSource, SqlPermission permission) {
        if (sqlDataSource != null && permission != null &&
                sqlDataSourceModelers != null && !sqlDataSourceModelers.isEmpty()) {
            SqlDataSourceModeler modeler = selectModeler(sqlDataSource.getVendor());
            SqlUser user = selectUser(sqlDataSource, permission);
            Objects.requireNonNull(user, "user with role %s not found".formatted(permission));
            return modeler.create(sqlDataSource, user, permission);
        } else if (sqlDataSource == null) {
            throw new IllegalArgumentException("SQL DataSource is not provided");
        } else if (permission == null) {
            throw new IllegalArgumentException("Permission is not provided");
        } else {
            throw new IllegalArgumentException("No data modelers specified");
        }
    }

    private SqlUser selectUser(SqlDataSource sqlDataSource,
                               SqlPermission permission) {
        SqlUser user = null;
        SqlDataSourceUsers users = sqlDataSource.getUsers();
        if (users != null) {
            List<? extends SqlUser> permittedUsers = null;
            if (permission.isSameAs(RO)) {
                permittedUsers = users.getReadOnly();
            } else if (permission.isSameAs(WO)) {
                permittedUsers = users.getWriteOnly();
            } else if (permission.isSameAs(RW)) {
                permittedUsers = users.getReadWrite();
            } else if (permission.isSameAs(RWX)) {
                permittedUsers = users.getReadWriteExecute();
            }
            if (permittedUsers != null && !permittedUsers.isEmpty()) {
                int size = permittedUsers.size();
                int userIndex = new Random().nextInt(size);
                user = permittedUsers.get(userIndex);
            }
        }
        return user;
    }

    private SqlDataSourceModeler selectModeler(SqlVendor vendor) {
        return sqlDataSourceModelers.stream()
                .filter(sqlDataSourceModeler -> sqlDataSourceModeler.getSqlVendor().isSameAs(vendor))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Sql modeler not registered for vendor: '%s'".formatted(vendor)
                ));
    }

}
