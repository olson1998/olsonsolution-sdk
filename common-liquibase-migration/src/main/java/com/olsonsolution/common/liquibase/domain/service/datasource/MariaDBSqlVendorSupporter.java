package com.olsonsolution.common.liquibase.domain.service.datasource;

import com.olsonsolution.common.data.domain.model.sql.SqlVendors;
import lombok.NonNull;
import org.mariadb.jdbc.MariaDbDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;

public class MariaDBSqlVendorSupporter extends AbstractSqlVendorSupporter {

    private static final String CREATE_SCHEMA_SQL = """
            CREATE DATABASE IF NOT EXISTS `%s`
              CHARACTER SET utf8mb4
              COLLATE utf8mb4_unicode_ci
            """;

    private static final String EXISTS_SCHEMA_SQL = """
            SELECT
              EXISTS (
                SELECT 1
                FROM INFORMATION_SCHEMA.SCHEMATA
                WHERE SCHEMA_NAME = ?
              ) AS database_exists
            """;

    public MariaDBSqlVendorSupporter() {
        super(SqlVendors.MARIADB, Collections.singletonList(MariaDbDataSource.class), Collections.emptyMap(),
                CREATE_SCHEMA_SQL, EXISTS_SCHEMA_SQL);
    }

    @Override
    public boolean existsSchema(@NonNull DataSource dataSource, String schema) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement schemaExistsStatement = connection.prepareStatement(EXISTS_SCHEMA_SQL)) {
            schemaExistsStatement.setString(1, schema);
            try (var resultSet = schemaExistsStatement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) == 1;
            }
        }
    }

}
