package com.olsonsolution.common.data.domain.service.sql;

import com.ibm.db2.jcc.DB2SimpleDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.olsonsolution.common.data.domain.model.exception.DataSourceModelerException;
import com.olsonsolution.common.data.domain.model.sql.SqlPermissions;
import com.olsonsolution.common.data.domain.model.sql.SqlVendors;
import com.olsonsolution.common.data.domain.port.stereotype.sql.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.osgi.PGDataSourceFactory;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.olsonsolution.common.data.domain.model.sql.SqlPermissions.*;
import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.*;
import static java.util.Map.entry;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class DataSourceModelerUtils {

    public static final Set<String> POSTGRESQL_PROPERTIES_NAMES = Set.of(
            "ssl",                        // Enable SSL (true/false)
            "sslmode",                    // SSL mode (disable, allow, prefer, require, verify-ca, verify-full)
            "sslfactory",                 // Custom SSLSocketFactory class name
            "sslcert",                    // Full path to the client's SSL certificate file
            "sslkey",                     // Full path to the client's SSL key file
            "sslrootcert",                // Full path to the SSL root certificate file
            "sslpassword",                // Password for the client's SSL key
            "sslhostnameverifier",        // Class name of a custom hostname verifier
            "sslpasswordcallback",        // Class name of the SSL password provider
            "sslnegotiation",             // SSL negotiation method (e.g., postgres or direct)
            "sendBufferSize",             // Socket write buffer size
            "receiveBufferSize",          // Socket read buffer size
            "tcpKeepAlive",               // Enable TCP keep-alive (true/false)
            "applicationName",            // Name of the application
            "assumeMinServerVersion",     // Assumes the server version for compatibility
            "preferQueryMode",            // Query execution mode (e.g., simple, extended, or extendedForPrepared)
            "reWriteBatchedInserts",      // Optimize batched inserts (true/false)
            "protocolVersion",            // Use a specific protocol version
            "stringtype",                 // Default handling of string types (e.g., varchar or unspecified)
            "autosave",                   // Behavior for saving transactions (always, conservative, or never)
            "socketTimeout",              // Timeout for socket operations (seconds)
            "connectTimeout",             // Timeout for establishing connections (seconds)
            "loginTimeout",               // Timeout for login (seconds)
            "defaultRowFetchSize",        // Default number of rows fetched per query
            "prepareThreshold",           // Number of PreparedStatement executions before switching to server-side
            "binaryTransfer",             // Use binary transfer for known types (true/false)
            "escapeSyntaxCallMode",       // Escape syntax call mode (e.g., select, call, or callIfNoReturn)
            "allowEncodingChanges",       // Allow changes to client_encoding (true/false)
            "jaasApplicationName",        // JAAS application name for Kerberos authentication
            "kerberosServerName",         // Kerberos service name
            "gssEncMode",                 // GSS encryption mode (disable, allow, prefer, require)
            "databaseMetadataCacheFields",// Number of fields to cache in DatabaseMetaData
            "databaseMetadataCacheFieldsMiB", // Memory size for caching fields in DatabaseMetaData (MiB)
            "loadBalanceHosts",           // Load balance between hosts (true/false)
            "hostRecheckSeconds",         // Seconds between host availability checks
            "unknownLength",              // Default length for unknown types
            "replication",                // Enable replication protocol (true/false)
            "preparedStatementCacheQueries", // Number of prepared statements to cache
            "preparedStatementCacheSizeMiB"  // Memory size for caching prepared statements (MiB)
    );

    private static final Map<String, Method> SQL_SERVER_PROPERTIES_SETTER_BOUNDS =
            Arrays.stream(SQLServerDataSource.class.getDeclaredMethods())
                    .filter(method -> StringUtils.startsWith(method.getName(), "set"))
                    .filter(method -> Void.TYPE.equals(method.getGenericReturnType()))
                    .filter(method -> method.getParameterCount() == 1)
                    .map(method -> entry(StringUtils.uncapitalize(method.getName()), method))
                    .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    private static final Map<String, Method> DB2_PROPERTIES_SETTER_BOUNDS =
            Arrays.stream(DB2SimpleDataSource.class.getDeclaredMethods())
                    .filter(method -> StringUtils.startsWith(method.getName(), "set"))
                    .filter(method -> Void.TYPE.equals(method.getGenericReturnType()))
                    .filter(method -> method.getParameterCount() == 1)
                    .map(method -> entry(StringUtils.uncapitalize(method.getName()), method))
                    .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    private static final Set<Type> PRIMITIVES =
            Set.of(Boolean.TYPE, Integer.TYPE, Double.TYPE, Short.TYPE, Long.TYPE, Float.TYPE);
    private static final PGDataSourceFactory POSTGRESQL_DATASOURCE_FACTORY = new PGDataSourceFactory();

    static SqlUser selectUserByPermission(SqlDataSource sqlDataSource, SqlPermission permission) {
        SqlDataSourceUsers users = sqlDataSource.getUsers();
        if (users == null) {
            throw new DataSourceModelerException("Users is null");
        }
        if (isReadOnly(permission)) {
            return selectRandom(users::getReadOnly, permission);
        } else if (isWriteOnly(permission)) {
            return selectRandom(users::getWriteOnly, permission);
        } else if (isReadWrite(permission)) {
            return selectRandom(users::getReadWrite, permission);
        } else if (isReadWriteExecute(permission)) {
            return selectRandom(users::getReadWriteExecute, permission);
        } else {
            throw new DataSourceModelerException("Unknown SQL permission: '%s'".formatted(permission));
        }
    }

    static DataSource createDataSource(@NonNull SqlDataSource sqlDataSource,
                                       SqlUser sqlUser,
                                       SqlPermission permission) {
        SqlVendor vendor = sqlDataSource.getVendor();
        if (vendor == null) {
            throw new DataSourceModelerException("Vendor is null");
        }
        if (vendor.isSqlServer()) {
            return createSQLServerDataSource(sqlDataSource, sqlUser);
        } else if (vendor.isPostgresql()) {
            return createPostgresqlDataSource(sqlDataSource, sqlUser, permission);
        } else if (vendor.isDb2()) {
            return createDb2DataSource(sqlDataSource, sqlUser, permission);
        } else {
            throw new DataSourceModelerException("Unknown SQL vendor: '%s'".formatted(vendor));
        }
    }

    private static SQLServerDataSource createSQLServerDataSource(@NonNull SqlDataSource sqlDataSource, SqlUser user) {
        SQLServerDataSource sqlServerDataSource = new SQLServerDataSource();
        Properties properties = sqlDataSource.getProperties();
        if (properties != null) {
            for (Map.Entry<String, Method> propertySetter : SQL_SERVER_PROPERTIES_SETTER_BOUNDS.entrySet()) {
                String property = propertySetter.getKey();
                if (properties.containsKey(property)) {
                    String propertyValueText = properties.getProperty(property);
                    Class<?> propertyType = propertySetter.getValue().getParameterTypes()[0];
                    Object propertyValue = DataSourceModelerUtils.convertPropertyValue(propertyValueText, propertyType);
                    try {
                        propertySetter.getValue().invoke(sqlServerDataSource, propertyValue);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new DataSourceModelerException("Failed to set SQL server property value", e);
                    }
                }
            }
        }
        sqlServerDataSource.setUser(user.getUsername());
        sqlServerDataSource.setPassword(user.getPassword());
        sqlServerDataSource.setServerName(sqlDataSource.getHost());
        sqlServerDataSource.setPortNumber(sqlDataSource.getPort());
        sqlServerDataSource.setDatabaseName(sqlDataSource.getDatabase());
        return sqlServerDataSource;
    }

    private static DataSource createPostgresqlDataSource(@NonNull SqlDataSource sqlDataSource,
                                                         SqlUser user,
                                                         SqlPermission permission) {
        Properties postgresDataSourceProperties = new Properties();
        Properties sqlDataSourceProperties = sqlDataSource.getProperties();
        if (sqlDataSourceProperties != null) {
            for (String property : POSTGRESQL_PROPERTIES_NAMES) {
                if (sqlDataSourceProperties.containsKey(property)) {
                    postgresDataSourceProperties.put(property, sqlDataSourceProperties.get(property));
                }
            }
        }
        postgresDataSourceProperties.put("serverName", sqlDataSource.getHost());
        postgresDataSourceProperties.put("portNumber", sqlDataSource.getPort());
        postgresDataSourceProperties.put("databaseName", sqlDataSource.getDatabase());
        postgresDataSourceProperties.put("user", user.getUsername());
        postgresDataSourceProperties.put("password", user.getPassword());
        if (isReadOnly(permission)) {
            postgresDataSourceProperties.put("readOnly", true);
        }
        try {
            return POSTGRESQL_DATASOURCE_FACTORY.createDataSource(postgresDataSourceProperties);
        } catch (SQLException e) {
            throw new DataSourceModelerException("Failed to create Postgresql Data Source", e);
        }
    }

    private static DB2SimpleDataSource createDb2DataSource(@NonNull SqlDataSource sqlDataSource,
                                                           SqlUser user,
                                                           SqlPermission permission) {
        DB2SimpleDataSource db2DataSource = new DB2SimpleDataSource();
        Properties sqlDataSourceProperties = sqlDataSource.getProperties();
        if(sqlDataSourceProperties != null) {
            for(Map.Entry<String, Method> propertySetter : DB2_PROPERTIES_SETTER_BOUNDS.entrySet()) {
                String property = propertySetter.getKey();
                if(sqlDataSourceProperties.containsKey(property)) {
                    String propertyValueString = sqlDataSourceProperties.getProperty(property);
                    Object propertyValue =
                            convertPropertyValue(propertyValueString, propertySetter.getValue().getParameterTypes()[0]);
                    try {
                        propertySetter.getValue().invoke(db2DataSource, propertyValue);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new DataSourceModelerException("Failed to set DB2 Data Source", e);
                    }
                }
            }
        }
        db2DataSource.setUser(user.getUsername());
        db2DataSource.setPassword(user.getPassword());
        db2DataSource.setServerName(sqlDataSource.getHost());
        db2DataSource.setPortNumber(sqlDataSource.getPort());
        db2DataSource.setDatabaseName(sqlDataSource.getDatabase());
        if(isReadOnly(permission)) {
            db2DataSource.setReadOnly(true);
        }
        return db2DataSource;
    }

    private static <V> V convertPropertyValue(String propertyValue, Class<V> convertedType) {
        Object convertedValue;
        if (PRIMITIVES.contains(convertedType) && propertyValue == null) {
            throw new DataSourceModelerException("Expected primitive type but got null value");
        }
        if (String.class.equals(convertedType) || Object.class.equals(convertedType)) {
            convertedValue = propertyValue;
        } else if (Boolean.TYPE.equals(convertedType) || Boolean.class.equals(convertedType)) {
            convertedValue = Boolean.valueOf(propertyValue);
        } else if (Integer.TYPE.equals(convertedType) || Integer.class.equals(convertedType)) {
            convertedValue = Integer.parseInt(propertyValue);
        } else if (Double.TYPE.equals(convertedType) || Double.class.equals(convertedType)) {
            convertedValue = Double.parseDouble(propertyValue);
        } else if (Short.TYPE.equals(convertedType) || Short.class.equals(convertedType)) {
            convertedValue = Short.parseShort(propertyValue);
        } else if (Long.TYPE.equals(convertedType) || Long.class.equals(convertedType)) {
            convertedValue = Long.parseLong(propertyValue);
        } else {
            throw new DataSourceModelerException("Not able to parse property to type=" + convertedType.getCanonicalName());
        }
        return convertedType.cast(convertedValue);
    }

    private static Optional<? extends SqlUser> findRandom(Supplier<Collection<? extends SqlUser>> userSupplier,
                                                          SqlPermission permission) {
        return Optional.ofNullable(userSupplier.get())
                .orElseGet(Collections::emptyList)
                .stream()
                .findAny();
    }

    private static SqlUser selectRandom(Supplier<Collection<? extends SqlUser>> userSupplier,
                                        SqlPermission permission) {
        return findRandom(userSupplier, permission)
                .orElseThrow(() -> new DataSourceModelerException(
                        "Data source does not provide any user with permission: '%s'".formatted(permission)));
    }

    private static boolean isReadOnly(SqlPermission permission) {
        if (permission instanceof SqlPermissions permissions) {
            return permissions == RO;
        } else {
            return StringUtils.equalsIgnoreCase(permission.name(), RO.name());
        }
    }

    private static boolean isWriteOnly(SqlPermission permission) {
        if (permission instanceof SqlPermissions permissions) {
            return permissions == WO;
        } else {
            return StringUtils.equalsIgnoreCase(permission.name(), WO.name());
        }
    }

    private static boolean isReadWrite(SqlPermission permission) {
        if (permission instanceof SqlPermissions permissions) {
            return permissions == RW;
        } else {
            return StringUtils.equalsIgnoreCase(permission.name(), RW.name());
        }
    }

    private static boolean isReadWriteExecute(SqlPermission permission) {
        if (permission instanceof SqlPermissions permissions) {
            return permissions == RWX;
        } else {
            return StringUtils.equalsIgnoreCase(permission.name(), RWX.name());
        }
    }

}
