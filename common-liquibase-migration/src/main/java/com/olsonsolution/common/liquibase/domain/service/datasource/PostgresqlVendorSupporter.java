package com.olsonsolution.common.liquibase.domain.service.datasource;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;

import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.POSTGRESQL;

public class PostgresqlVendorSupporter extends AbstractSqlVendorSupporter {

    private static final String CREATE_SCHEMA_SQL = "CREATE SCHEMA %s";

    private static final String EXISTS_SCHEMA_SQL = """
            SELECT CASE WHEN EXISTS (
                SELECT 1 FROM information_schema.schemata WHERE schema_name = '%s'
            ) THEN 1 ELSE 0 END;
            """;

    private static final Collection<Class<? extends DataSource>> POSTGRESQL_SUPPORTED_DATA_SOURCE_CLASSES =
            Collections.singletonList(PGSimpleDataSource.class);

    public PostgresqlVendorSupporter() {
        super(
                POSTGRESQL, POSTGRESQL_SUPPORTED_DATA_SOURCE_CLASSES,
                Collections.emptyMap(), CREATE_SCHEMA_SQL, EXISTS_SCHEMA_SQL
        );
    }
}
