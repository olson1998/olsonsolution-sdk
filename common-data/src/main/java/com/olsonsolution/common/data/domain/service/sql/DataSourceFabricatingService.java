package com.olsonsolution.common.data.domain.service.sql;

import com.olsonsolution.common.data.domain.port.repository.sql.SqlDataSourceFactory;
import com.olsonsolution.common.data.domain.port.repository.sql.SqlDataSourceModeler;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class DataSourceFabricatingService implements SqlDataSourceFactory {

    private final List<SqlDataSourceModeler> sqlDataSourceModelers;

    @Override
    public DataSource fabricate(SqlDataSource sqlDataSource, SqlPermission permission) {
        if (sqlDataSource != null && permission != null &&
                sqlDataSourceModelers != null && !sqlDataSourceModelers.isEmpty()) {
            SqlDataSourceModeler modeler = selectModeler(sqlDataSource.getVendor());
            SqlUser user = sqlDataSource.getUser();
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

    private SqlDataSourceModeler selectModeler(SqlVendor vendor) {
        return sqlDataSourceModelers.stream()
                .filter(sqlDataSourceModeler -> sqlDataSourceModeler.getSqlVendor().isSameAs(vendor))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Sql modeler not registered for vendor: '%s'".formatted(vendor)
                ));
    }

}
