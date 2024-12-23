package com.olsonsolution.common.data.domain.service.sql;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import lombok.extern.slf4j.Slf4j;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.H2;

@Slf4j
public class H2DataSourceModeler extends AbstractDataSourceModeler {

    private static final Map<String, String> H2_QUERY_PROPERTIES = Map.ofEntries(
            Map.entry("DB_CLOSE_DELAY", "Controls when the database is closed automatically."),
            Map.entry("DB_CLOSE_ON_EXIT", "Determines if the database should close when the JVM exits."),
            Map.entry("AUTO_RECONNECT", "Enables automatic reconnection if the connection is lost."),
            Map.entry("MODE", "Sets the compatibility mode with other databases (e.g., PostgreSQL, MySQL)."),
            Map.entry("CACHE_SIZE", "Specifies the size of the cache in KB."),
            Map.entry("TRACE_LEVEL_FILE", "Sets the trace level for file output."),
            Map.entry("TRACE_LEVEL_SYSTEM_OUT", "Sets the trace level for System.out."),
            Map.entry("MV_STORE", "Enables the MVStore storage engine."),
            Map.entry("MVCC", "Enables Multi-Version Concurrency Control."),
            Map.entry("ACCESS_MODE_DATA", "Sets the access mode for data (e.g., read-only)."),
            Map.entry("IGNORECASE", "Makes all identifiers case-insensitive."),
            Map.entry("AUTO_SERVER", "Allows automatic mixed-mode access."),
            Map.entry("AUTO_SERVER_PORT", "Specifies the port for the automatic mixed-mode."),
            Map.entry("AUTO_SERVER_SSL", "Enables SSL for automatic mixed-mode."),
            Map.entry("FILE_LOCK", "Sets the file locking mode (e.g., FILE, NO, SERIALIZED)."),
            Map.entry("PAGE_SIZE", "Defines the page size for the database."),
            Map.entry("CACHE_TYPE", "Specifies the cache type (e.g., LRU, SOFT_LRU)."),
            Map.entry("DEFRAG_ALWAYS", "Enables automatic defragmentation."),
            Map.entry("DATABASE_TO_UPPER", "Converts all identifiers to uppercase."),
            Map.entry("DEFAULT_ESCAPE", "Sets the default escape character."),
            Map.entry("IFEXISTS", "Checks if the database exists before connecting."),
            Map.entry("INIT", "Runs a SQL statement at the time of database initialization."),
            Map.entry("PASSWORD_HASH", "Enables password hashing."),
            Map.entry("SCRIPT_FORMAT", "Sets the format for script files (e.g., 0 for SQL, 1 for binary)."),
            Map.entry("TRACE_MAX_FILE_SIZE", "Specifies the maximum size of trace files."),
            Map.entry("WRITE_DELAY", "Sets the write delay for disk operations.")
    );

    public H2DataSourceModeler() {
        super(H2, log, Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public DataSource create(SqlDataSource dataSource, SqlUser user, SqlPermission permission) {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL(writeURL(dataSource));
        h2DataSource.setUser(user.getUsername());
        h2DataSource.setPassword(user.getPassword());
        return h2DataSource;
    }

    private String writeURL(SqlDataSource dataSource) {
        StringBuilder url = new StringBuilder("jdbc:h2:");
        url.append(dataSource.getHost()).append(':').append(dataSource.getDatabase()).append(';');
        Map<String, String> properties = dataSource.getProperties();
        if(properties != null && !properties.isEmpty()) {

        }
        return url.toString();
    }

}
