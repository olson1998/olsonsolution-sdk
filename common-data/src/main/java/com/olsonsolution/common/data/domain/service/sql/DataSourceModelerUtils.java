package com.olsonsolution.common.data.domain.service.sql;

import com.ibm.db2.jcc.DB2SimpleDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.postgresql.ds.PGSimpleDataSource;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class DataSourceModelerUtils {

    public static SQLServerDataSource createSQLServerDataSource(SqlDataSource dataSource, SqlUser user, SqlPermission perm) {
        SQLServerDataSource sqlServerDataSource = new SQLServerDataSource();
        sqlServerDataSource.setServerName(dataSource.getHost());
        Optional.ofNullable(dataSource.getPort()).ifPresent(sqlServerDataSource::setPortNumber);
        sqlServerDataSource.setUser(user.getUsername());
        sqlServerDataSource.setPassword(user.getPassword());
        sqlServerDataSource.setDatabaseName(dataSource.getDatabase());
        sqlServerDataSource.setTrustServerCertificate(true);
        sqlServerDataSource.setEncrypt("false");
        return sqlServerDataSource;
    }

    public static PGSimpleDataSource createPostgresDataSource(SqlDataSource dataSource, SqlUser user, SqlPermission perm) {
        PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
        pgSimpleDataSource.setServerNames(new String[]{dataSource.getHost()});
        Optional.ofNullable(dataSource.getPort()).ifPresent(pgSimpleDataSource::setPortNumber);
        pgSimpleDataSource.setDatabaseName(dataSource.getDatabase());
        pgSimpleDataSource.setUser(user.getUsername());
        pgSimpleDataSource.setPassword(user.getPassword());
        return pgSimpleDataSource;
    }

    public static DB2SimpleDataSource createDb2DataSource(SqlDataSource dataSource, SqlUser user, SqlPermission perm) {
        DB2SimpleDataSource db2SimpleDataSource = new DB2SimpleDataSource();
        db2SimpleDataSource.setServerName(dataSource.getHost());
        Optional.ofNullable(dataSource.getPort()).ifPresent(db2SimpleDataSource::setPortNumber);
        db2SimpleDataSource.setDatabaseName(dataSource.getDatabase());
        db2SimpleDataSource.setUser(user.getUsername());
        db2SimpleDataSource.setPassword(user.getPassword());
        return db2SimpleDataSource;
    }

}
