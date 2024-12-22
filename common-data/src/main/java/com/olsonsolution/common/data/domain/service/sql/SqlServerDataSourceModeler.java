package com.olsonsolution.common.data.domain.service.sql;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import com.olsonsolution.common.property.domain.port.stereotype.PropertySpec;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.*;

import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.SQL_SERVER;

@Slf4j
public class SqlServerDataSourceModeler extends AbstractDataSourceModeler {

    private static final List<Map.Entry<PropertySpec, Method>> PROPERTY_SETTERS =
            AbstractDataSourceModeler.loadPropertySpecSetters(SQLServerDataSource.class);

    private static final Collection<? extends PropertySpec> PROPERTIES = PROPERTY_SETTERS
            .stream()
            .map(Map.Entry::getKey)
            .toList();

    public SqlServerDataSourceModeler() {
        super(SQL_SERVER, log, Collections.emptyList(), PROPERTIES);
    }

    @Override
    public DataSource create(SqlDataSource dataSource, SqlUser user, SqlPermission permission) {
        SQLServerDataSource sqlServerDataSource = new SQLServerDataSource();
        sqlServerDataSource.setServerName(sqlServerDataSource.getServerName());
        Optional.ofNullable(dataSource.getPort()).ifPresent(sqlServerDataSource::setPortNumber);
        sqlServerDataSource.setDatabaseName(dataSource.getDatabase());
        sqlServerDataSource.setUser(user.getUsername());
        sqlServerDataSource.setPassword(user.getPassword());
        loadProperties(sqlServerDataSource, dataSource, PROPERTY_SETTERS);
        return sqlServerDataSource;
    }
}
