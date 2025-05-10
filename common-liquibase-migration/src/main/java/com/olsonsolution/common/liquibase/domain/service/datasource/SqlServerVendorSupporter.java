package com.olsonsolution.common.liquibase.domain.service.datasource;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;

import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.SQL_SERVER;

public class SqlServerVendorSupporter extends AbstractSqlVendorSupporter {

    private static final String CREATE_SCHEMA_SQL = "CREATE SCHEMA %s";

    private static final String EXISTS_SCHEMA_SQL = """
            SELECT CASE WHEN EXISTS (
                SELECT 1 FROM sys.schemas WHERE name = '%s'
            ) THEN 1 ELSE 0 END;
            """;

    private static final Collection<Class<? extends DataSource>> SQL_SERVER_SUPPORTED_DATA_SOURCE_CLASSES =
            Collections.singletonList(SQLServerDataSource.class);

    public SqlServerVendorSupporter() {
        super(
                SQL_SERVER, SQL_SERVER_SUPPORTED_DATA_SOURCE_CLASSES, Collections.emptyMap(),
                CREATE_SCHEMA_SQL, EXISTS_SCHEMA_SQL
        );
    }
}
