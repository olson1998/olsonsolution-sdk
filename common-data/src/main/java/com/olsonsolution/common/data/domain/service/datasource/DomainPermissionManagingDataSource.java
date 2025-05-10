package com.olsonsolution.common.data.domain.service.datasource;

import com.olsonsolution.common.data.domain.port.datasource.PermissionManagingDataSource;
import com.olsonsolution.common.data.domain.port.datasource.SqlPermissionProvider;
import com.olsonsolution.common.data.domain.port.datasource.SqlVendorAwareDataSource;
import com.olsonsolution.common.data.domain.port.repository.sql.SqlDataSourceFactory;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

@Slf4j
@RequiredArgsConstructor
public class DomainPermissionManagingDataSource implements PermissionManagingDataSource, SqlVendorAwareDataSource {

    @Getter
    private boolean closed;

    private final SqlDataSource sqlDataSource;

    private final SqlDataSourceFactory sqlDataSourceFactory;

    private final SqlPermissionProvider sqlPermissionProvider;

    private final ConcurrentMap<SqlPermission, DataSource> dataSourceRegistry = new ConcurrentHashMap<>();

    @Override
    public DataSource getByPermission(SqlPermission permission) {
        return dataSourceRegistry.computeIfAbsent(
                permission,
                p -> sqlDataSourceFactory.fabricate(sqlDataSource, p)
        );
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getThreadLocal().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getThreadLocal().getConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return getThreadLocal().getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        getThreadLocal().setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        getThreadLocal().setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return getThreadLocal().getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return getThreadLocal().getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return getThreadLocal().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return getThreadLocal().isWrapperFor(iface);
    }

    private DataSource getThreadLocal() {
        if (closed) {
            throw new IllegalStateException("DataSource has been closed");
        }
        SqlPermission permission = sqlPermissionProvider.getThreadLocalPermission();
        return getByPermission(permission);
    }

    @Override
    public void close() {
        closed = true;
        Iterator<Map.Entry<SqlPermission, DataSource>> dataSourceIterator = dataSourceRegistry.entrySet().iterator();
        while (dataSourceIterator.hasNext()) {
            Map.Entry<SqlPermission, DataSource> dataSourceWithPermission = dataSourceIterator.next();
            SqlPermission permission = dataSourceWithPermission.getKey();
            DataSource dataSource = dataSourceWithPermission.getValue();
            if (dataSource instanceof AutoCloseable closeableDataSource) {
                try {
                    closeableDataSource.close();
                    log.debug("Data source permission: '{}' closed", permission);
                } catch (Exception e) {
                    log.error("Data source permission: '{}' has not been closed, reason:", permission, e);
                }
            }
        }
    }

    @Override
    public SqlVendor getVendor() {
        return sqlDataSource.getVendor();
    }
}
