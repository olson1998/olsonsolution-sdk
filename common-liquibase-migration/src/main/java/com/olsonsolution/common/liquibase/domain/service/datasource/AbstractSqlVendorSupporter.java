package com.olsonsolution.common.liquibase.domain.service.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.migration.domain.port.repository.SqlVendorSupporter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
abstract class AbstractSqlVendorSupporter implements SqlVendorSupporter {

    @Getter
    private final SqlVendor vendor;

    @Getter
    private final Collection<Class<? extends DataSource>> supportedDataSourceClasses;

    @Getter
    private final Map<String, String> typeVariables;

    private final String createSchemaSql;

    private final String existsSchemaSql;

    @Override
    public boolean existsSchema(@NonNull DataSource dataSource, String schema) throws SQLException {
        String sql = existsSchemaSql.formatted(schema);
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1) == 1;
        }
    }

    @Override
    public void createSchema(@NonNull DataSource dataSource, String schema) throws SQLException {
        String sql = createSchemaSql.formatted(schema);
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }

}
