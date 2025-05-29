package com.olsonsolution.common.data.domain.service.sql;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import com.olsonsolution.common.data.domain.service.datasource.MariaDbDataSourceWrapper;
import com.olsonsolution.common.property.domain.model.PropertySpecModel;
import com.olsonsolution.common.property.domain.port.stereotype.PropertySpec;
import lombok.extern.slf4j.Slf4j;
import org.mariadb.jdbc.MariaDbDataSource;

import javax.sql.DataSource;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.MARIADB;
import static java.util.Map.entry;

@Slf4j
public class MariaDbDataSourceModeler extends AbstractDataSourceModeler {

    public static final Map<String, String> PROPERTIES = Map.ofEntries(
            entry("serverTimezone", "Time zone of the database server."),
            entry("useUnicode", "Enable Unicode character encoding. Default is true."),
            entry("characterEncoding", "Character encoding to use, such as UTF-8."),
            entry("connectionTimeout", "Timeout in milliseconds before a connection attempt is aborted. Default is 30000."),
            entry("socketTimeout", "Timeout in milliseconds for socket read operations. Default is 0 (infinite)."),
            entry("autoReconnect", "Enable automatic reconnection on connection loss. Default is false."),
            entry("maxReconnects", "Maximum number of reconnect attempts if autoReconnect is enabled. Default is 3."),
            entry("useCompression", "Enable compression of data sent between client and server. Default is false."),
            entry("allowMultiQueries", "Allow execution of multiple queries in a single statement. Default is false."),
            entry("rewriteBatchedStatements", "Enable rewriting of batched statements for performance optimization. Default is false."),
            entry("useServerPrepStmts", "Use server-side prepared statements. Default is false."),
            entry("cachePrepStmts", "Enable caching of prepared statements. Default is false."),
            entry("prepStmtCacheSize", "Number of prepared statements to cache if cachePrepStmts is enabled. Default is 25."),
            entry("prepStmtCacheSqlLimit", "Maximum length of prepared SQL statements to cache. Default is 256."),
            entry("useSSL", "Enable SSL for the connection. Default is false."),
            entry("requireSSL", "Require SSL for the connection; connection fails if SSL is not available. Default is false."),
            entry("verifyServerCertificate", "Verify the server's SSL certificate. Default is true."),
            entry("trustCertificateKeyStoreUrl", "URL to the truststore file containing the server's certificate."),
            entry("trustCertificateKeyStorePassword", "Password for the truststore file."),
            entry("clientCertificateKeyStoreUrl", "URL to the keystore file containing the client's certificate."),
            entry("clientCertificateKeyStorePassword", "Password for the client keystore file."),
            entry("serverSslCert", "Path to the server's public certificate file."),
            entry("allowPublicKeyRetrieval", "Allow retrieval of the public key from the server for authentication. Default is false."),
            entry("useReadAheadInput", "Use a buffered input stream to read socket data, improving performance."),
            entry("pool", "Enable connection pooling. Default is false."),
            entry("maxPoolSize", "Maximum number of connections in the pool. Default is 8."),
            entry("minPoolSize", "Minimum number of connections to keep in the pool."),
            entry("maxIdleTime", "Maximum time in seconds that a connection can remain idle in the pool. Default is 600."),
            entry("poolValidMinDelay", "Minimum delay in milliseconds between connection validations in the pool. Default is 1000."),
            entry("poolName", "Name of the connection pool."),
            entry("useResetConnection", "Reset connection state when returning to the pool. Default is false."),
            entry("permitMysqlScheme", "Allow the use of jdbc:mysql: scheme in connection URLs."),
            entry("transactionReplay", "Enable transaction replay after a failover. Default is false."),
            entry("transactionReplaySize", "Maximum number of statements to cache for transaction replay."),
            entry("authenticationPlugins", "Comma-separated list of authentication plugins to use."),
            entry("disabledAuthenticationPlugins", "Comma-separated list of authentication plugins to disable."),
            entry("defaultAuthenticationPlugin", "Default authentication plugin to use."),
            entry("ociConfigFile", "Path to the Oracle Cloud Infrastructure configuration file."),
            entry("ociConfigProfile", "Profile name in the OCI configuration file to use. Default is DEFAULT."),
            entry("fallbackToSystemKeyStore", "Fallback to the system keystore for client certificates if not specified."),
            entry("fallbackToSystemTrustStore", "Fallback to the system truststore for server certificates if not specified."),
            entry("useConfigs", "Load predefined configurations."),
            entry("clientInfoProvider", "Class name of the client info provider."),
            entry("createDatabaseIfNotExist", "Create the database if it does not exist. Default is false."),
            entry("databaseTerm", "Term to use for the database (CATALOG or SCHEMA). Default is CATALOG."),
            entry("detectCustomCollations", "Detect custom collations on the server. Default is false."),
            entry("disconnectOnExpiredPasswords", "Disconnect when the password has expired. Default is true."),
            entry("interactiveClient", "Use interactive client mode. Default is false."),
            entry("passwordCharacterEncoding", "Character encoding for the password."),
            entry("propertiesTransform", "Class name of the properties transformer."),
            entry("rollbackOnPooledClose", "Rollback pending transactions when a pooled connection is closed. Default is true."),
            entry("useAffectedRows", "Return the number of affected rows instead of matched rows. Default is false."),
            entry("metadataCacheSize", "Size of the metadata cache if cacheResultSetMetadata is enabled. Default is 50.")
    );

    private static final Collection<? extends PropertySpec> PROPERTIES_SPEC = PROPERTIES.entrySet()
            .stream()
            .map(propertyDesc -> PropertySpecModel.propertySpec()
                    .name(propertyDesc.getKey())
                    .description(propertyDesc.getValue())
                    .build())
            .toList();

    public MariaDbDataSourceModeler() {
        super(MARIADB, log, Collections.emptyList(), PROPERTIES_SPEC);
    }

    @Override
    public DataSource create(SqlDataSource dataSource, SqlUser user, SqlPermission permission) {
        MariaDbDataSource mariaDbDataSource = new MariaDbDataSourceWrapper();
        String url = writeURL(dataSource);
        try {
            mariaDbDataSource.setUrl(url);
        } catch (SQLException e) {
            logError("JDBC URL", e);
        }
        try {
            mariaDbDataSource.setUser(user.getUsername());
        } catch (SQLException e) {
            logError("user", e);
        }
        try {
            mariaDbDataSource.setPassword(user.getPassword());
        } catch (SQLException e) {
            logError("password", e);
        }
        return mariaDbDataSource;
    }

    private void logError(String property, SQLException e) {
        log.warn("Failed to set {} reason:", property, e);
    }

    private String writeURL(SqlDataSource dataSource) {
        StringBuilder url = new StringBuilder("jdbc:mariadb://")
                .append(dataSource.getHost());
        Optional.ofNullable(dataSource.getPort()).ifPresent(port -> url.append(':').append(port));
        Optional.ofNullable(dataSource.getDatabase()).ifPresent(db -> url.append('/').append(db));
        Map<String, String> properties = dataSource.getProperties();
        if (properties != null && !properties.isEmpty()) {
            url.append('?');
            for (Map.Entry<String, String> propertyValue : properties.entrySet()) {
                String property = propertyValue.getKey();
                String encodedValue = URLEncoder.encode(propertyValue.getValue());
                url.append(property).append('=').append(encodedValue);
            }
        }
        return url.toString();
    }

}
