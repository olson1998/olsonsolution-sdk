package com.olsonsolution.common.liquibase.domain.service.datasource;

import com.olsonsolution.common.data.domain.model.sql.SqlVendors;
import com.olsonsolution.common.data.domain.port.datasource.PermissionManagingDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSourceUsers;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.mariadb.jdbc.MariaDbDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

public class MariaDBSqlVendorSupporter extends AbstractSqlVendorSupporter {

    private static final String CREATE_SCHEMA_SQL = """
            CREATE DATABASE IF NOT EXISTS ?
              CHARACTER SET utf8mb4
              COLLATE utf8mb4_unicode_ci;
            """;

    private static final String GRANT_PRIVILEGES_SQL = """
            GRANT ALL
              ON ?.*\s
              TO ?%'
              IDENTIFIED BY ?
            """;

    private static final String FLUSH_PRIVILEGES = "FLUSH PRIVILEGES";

    private static final String EXISTS_SCHEMA_SQL = """
            SELECT
              EXISTS (
                SELECT 1
                FROM INFORMATION_SCHEMA.SCHEMATA
                WHERE SCHEMA_NAME = ?
              ) AS database_exists;
            """;

    public MariaDBSqlVendorSupporter() {
        super(SqlVendors.MARIADB, Collections.singletonList(MariaDbDataSource.class), Collections.emptyMap(),
                CREATE_SCHEMA_SQL, EXISTS_SCHEMA_SQL);
    }

    @Override
    public boolean existsSchema(@NonNull DataSource dataSource, String schema) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement existsSchemaStatement = connection.prepareStatement(EXISTS_SCHEMA_SQL)) {
            existsSchemaStatement.setString(1, schema);
            try (ResultSet resultSet = existsSchemaStatement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) == 1;
            }
        }
    }

    @Override
    public void createSchema(@NonNull DataSource dataSource, String schema) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement createDbStatement = connection.prepareStatement(CREATE_SCHEMA_SQL)) {
            createDbStatement.setString(1, schema);
            if (dataSource instanceof PermissionManagingDataSource permissionManagingDataSource) {
                SqlDataSource sqlDataSource = permissionManagingDataSource.getSqlDataSource();
                SqlDataSourceUsers sqlDataSourceUsers = sqlDataSource.getUsers();
                for (SqlUser user : sqlDataSourceUsers.getReadWriteExecute()) {
                    if (StringUtils.equals(user.getSchema(), schema)) {
                        grantPrivileges(connection, user, schema);
                    }
                }
            }
        }
    }

    private void grantPrivileges(Connection connection, SqlUser sqlUser, String schema) throws SQLException {
        try (PreparedStatement grantPrivilegesStatement = connection.prepareStatement(GRANT_PRIVILEGES_SQL);
             PreparedStatement flushPrivilegesStatement = connection.prepareStatement(FLUSH_PRIVILEGES)) {
            grantPrivilegesStatement.setString(1, schema);
            grantPrivilegesStatement.setString(2, sqlUser.getUsername());
            grantPrivilegesStatement.setString(3, sqlUser.getPassword());
            grantPrivilegesStatement.execute();
            flushPrivilegesStatement.execute();
        }
    }
}
