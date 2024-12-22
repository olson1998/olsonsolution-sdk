package com.olsonsolution.common.data.domain.model.sql;

import com.olsonsolution.common.property.domain.model.BooleanPropertySpec;
import com.olsonsolution.common.property.domain.model.EnumPropertySpec;
import com.olsonsolution.common.property.domain.model.PropertySpecModel;
import com.olsonsolution.common.property.domain.port.stereotype.PropertySpec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.postgresql.jdbc.AutoSave;
import org.postgresql.jdbc.PreferQueryMode;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgresDataSourceProperties {

    public static final PropertySpec OPTIONS = PropertySpecModel.propertySpec()
            .name("options")
            .description("Sets command-line options for the connection.")
            .build();

    public static final PropertySpec LOGIN_TIMEOUT = PropertySpecModel.propertySpec()
            .name("loginTimeout")
            .type(Integer.TYPE)
            .description("Sets the login timeout.")
            .build();

    public static final PropertySpec CONNECT_TIMEOUT = PropertySpecModel.propertySpec()
            .name("connectTimeout")
            .type(Integer.TYPE)
            .description("Sets the connection timeout.")
            .build();

    public static final PropertySpec GSS_RESPONSE_TIMEOUT = PropertySpecModel.propertySpec()
            .name("gssResponseTimeout")
            .type(Integer.TYPE)
            .description("Sets the GSS response timeout.")
            .build();

    public static final PropertySpec SSL_RESPONSE_TIMEOUT = PropertySpecModel.propertySpec()
            .name("sslResponseTimeout")
            .type(Integer.TYPE)
            .description("Sets the SSL response timeout.")
            .build();

    public static final PropertySpec PROTOCOL_VERSION = PropertySpecModel.propertySpec()
            .name("protocolVersion")
            .type(Integer.TYPE)
            .description("Specifies the protocol version.")
            .build();

    public static final PropertySpec QUOTE_RETURNING_IDENTIFIERS = BooleanPropertySpec.booleanPropertySpec()
            .name("quoteReturningIdentifiers")
            .description("Specifies whether returning identifiers should be quoted.")
            .build();

    public static final PropertySpec RECEIVE_BUFFER_SIZE = PropertySpecModel.propertySpec()
            .name("receiveBufferSize")
            .type(Integer.TYPE)
            .description("Configures the receive buffer size.")
            .build();

    public static final PropertySpec SEND_BUFFER_SIZE = PropertySpecModel.propertySpec()
            .name("sendBufferSize")
            .type(Integer.TYPE)
            .description("Configures the send buffer size.")
            .build();

    public static final PropertySpec PREPARED_STATEMENT_CACHE_QUERIES = PropertySpecModel.propertySpec()
            .name("preparedStatementCacheQueries")
            .type(Integer.TYPE)
            .description("Sets the cache size for prepared statements.")
            .build();

    public static final PropertySpec SOCKET_TIMEOUT = PropertySpecModel.propertySpec()
            .name("socketTimeout")
            .type(Integer.TYPE)
            .description("Specifies the socket timeout.")
            .build();

    public static final PropertySpec SSL = BooleanPropertySpec.booleanPropertySpec()
            .name("ssl")
            .description("Enables or disables SSL.")
            .build();

    public static final PropertySpec SSLFACTORY = PropertySpecModel.propertySpec()
            .name("sslfactory")
            .description("Configures the SSL factory class.")
            .build();

    public static final PropertySpec SSL_CERT = PropertySpecModel.propertySpec()
            .name("sslCert")
            .description("Specifies the SSL certificate file.")
            .build();

    public static final PropertySpec SSL_KEY = PropertySpecModel.propertySpec()
            .name("sslKey")
            .description("Specifies the SSL key file.")
            .build();

    public static final PropertySpec APPLICATION_NAME = PropertySpecModel.propertySpec()
            .name("applicationName")
            .description("Sets the application name.")
            .build();

    public static final PropertySpec BINARY_TRANSFER = BooleanPropertySpec.booleanPropertySpec()
            .name("binaryTransfer")
            .description("Enables or disables binary transfer.")
            .build();

    public static final PropertySpec ADAPTIVE_FETCH = BooleanPropertySpec.booleanPropertySpec()
            .name("adaptiveFetch")
            .description("Enables or disables adaptive fetch.")
            .build();

    public static final PropertySpec PREFER_QUERY_MODE = EnumPropertySpec.enumPropertySpec()
            .name("preferQueryMode")
            .type(PreferQueryMode.class)
            .description("Sets the preferred query mode.")
            .build();

    public static final PropertySpec AUTOSAVE = EnumPropertySpec.enumPropertySpec()
            .name("autosave")
            .type(AutoSave.class)
            .description("Configures automatic per-query savepoints.")
            .build();

    public static final PropertySpec CLEANUP_SAVEPOINTS = BooleanPropertySpec.booleanPropertySpec()
            .name("cleanupSavepoints")
            .description("Indicates whether to clean up savepoints after successful transactions.")
            .build();

    public static final PropertySpec TCP_KEEP_ALIVE = BooleanPropertySpec.booleanPropertySpec()
            .name("tcpKeepAlive")
            .description("Enables or disables TCP keep-alive.")
            .build();

    public static final PropertySpec TCP_NO_DELAY = BooleanPropertySpec.booleanPropertySpec()
            .name("tcpNoDelay")
            .description("Enables or disables TCP no-delay.")
            .build();

    public static final PropertySpec REWRITE_BATCHED_INSERTS = BooleanPropertySpec.booleanPropertySpec()
            .name("reWriteBatchedInserts")
            .description("Enables rewriting batched inserts.")
            .build();

    public static final PropertySpec LOCAL_SOCKET_ADDRESS = PropertySpecModel.propertySpec()
            .name("localSocketAddress")
            .description("Configures the local socket address.")
            .build();

    public static final PropertySpec LOAD_BALANCE_HOSTS = BooleanPropertySpec.booleanPropertySpec()
            .name("loadBalanceHosts")
            .description("Enables or disables host load balancing.")
            .build();

    public static final PropertySpec HOST_RECHECK_SECONDS = PropertySpecModel.propertySpec()
            .name("hostRecheckSeconds")
            .type(Integer.TYPE)
            .description("Sets the recheck interval for host availability.")
            .build();

    public static final PropertySpec STRING_TYPE = PropertySpecModel.propertySpec()
            .name("stringType")
            .description("Specifies the string type for the connection.")
            .build();

    public static final PropertySpec REPLICATION = PropertySpecModel.propertySpec()
            .name("replication")
            .description("Configures replication settings.")
            .build();

    public static final PropertySpec GSS_LIB = PropertySpecModel.propertySpec()
            .name("gssLib")
            .description("Specifies the GSS library.")
            .build();

    public static final PropertySpec GSS_ENC_MODE = PropertySpecModel.propertySpec()
            .name("gssEncMode")
            .description("Configures the GSS encryption mode.")
            .build();

    public static final PropertySpec READ_ONLY = BooleanPropertySpec.booleanPropertySpec()
            .name("readOnly")
            .description("Sets the connection to read-only mode.")
            .build();

    public static final PropertySpec LOG_UNCLOSED_CONNECTIONS = BooleanPropertySpec.booleanPropertySpec()
            .name("logUnclosedConnections")
            .description("Logs unclosed connections if enabled.")
            .build();

    public static final PropertySpec ALLOW_ENCODING_CHANGES = BooleanPropertySpec.booleanPropertySpec()
            .name("allowEncodingChanges")
            .description("Enables or disables encoding changes.")
            .build();

    public static final PropertySpec SOCKET_FACTORY = PropertySpecModel.propertySpec()
            .name("socketFactory")
            .description("Configures the socket factory class.")
            .build();

    public static final PropertySpec SSL_MODE = PropertySpecModel.propertySpec()
            .name("sslMode")
            .description("Configures the SSL mode.")
            .build();

    public static final PropertySpec SSL_PASSWORD_CALLBACK = PropertySpecModel.propertySpec()
            .name("sslPasswordCallback")
            .description("Configures the SSL password callback class.")
            .build();

    public static final PropertySpec GROUP_STARTUP_PARAMETERS = BooleanPropertySpec.booleanPropertySpec()
            .name("groupStartupParameters")
            .description("Specifies whether to group startup parameters.")
            .build();

    public static final PropertySpec SSL_PASSWORD = PropertySpecModel.propertySpec()
            .name("sslPassword")
            .description("Sets the SSL password.")
            .build();

    public static final PropertySpec SSL_HOSTNAME_VERIFIER = PropertySpecModel.propertySpec()
            .name("sslHostnameVerifier")
            .description("Configures the SSL hostname verifier class.")
            .build();

    public static final PropertySpec SSL_FACTORY_ARG = PropertySpecModel.propertySpec()
            .name("sslFactoryArg")
            .description("Sets the argument for the SSL factory.")
            .build();

    public static final PropertySpec DATABASE_METADATA_CACHE_FIELDS = PropertySpecModel.propertySpec()
            .name("databaseMetadataCacheFields")
            .type(Integer.TYPE)
            .description("Sets the number of fields in the database metadata cache.")
            .build();

    public static final PropertySpec DATABASE_METADATA_CACHE_FIELDS_MIB = PropertySpecModel.propertySpec()
            .name("databaseMetadataCacheFieldsMiB")
            .type(Integer.TYPE)
            .description("Sets the size of the database metadata cache in MiB.")
            .build();

    public static final PropertySpec DEFAULT_ROW_FETCH_SIZE = PropertySpecModel.propertySpec()
            .name("defaultRowFetchSize")
            .type(Integer.TYPE)
            .description("Sets the default row fetch size.")
            .build();

    public static final PropertySpec ADAPTIVE_FETCH_MAXIMUM = PropertySpecModel.propertySpec()
            .name("adaptiveFetchMaximum")
            .type(Integer.TYPE)
            .description("Sets the maximum value for adaptive fetch.")
            .build();

    public static final PropertySpec ADAPTIVE_FETCH_MINIMUM = PropertySpecModel.propertySpec()
            .name("adaptiveFetchMinimum")
            .type(Integer.TYPE)
            .description("Sets the minimum value for adaptive fetch.")
            .build();

    public static final PropertySpec ESCAPE_SYNTAX_CALL_MODE = PropertySpecModel.propertySpec()
            .name("escapeSyntaxCallMode")
            .description("Configures the escape syntax call mode.")
            .build();

    public static final PropertySpec MAX_SEND_BUFFER_SIZE = PropertySpecModel.propertySpec()
            .name("maxSendBufferSize")
            .type(Integer.TYPE)
            .description("Sets the maximum send buffer size.")
            .build();

    public static final PropertySpec PREPARE_THRESHOLD = PropertySpecModel.propertySpec()
            .name("prepareThreshold")
            .type(Integer.TYPE)
            .description("Configures the prepare threshold.")
            .build();

    public static final PropertySpec PREPARED_STATEMENT_CACHE_SIZE_MIB = PropertySpecModel.propertySpec()
            .name("preparedStatementCacheSizeMiB")
            .type(Integer.TYPE)
            .description("Sets the size of the prepared statement cache in MiB.")
            .build();

    public static final PropertySpec SSL_ROOT_CERT = PropertySpecModel.propertySpec()
            .name("sslRootCert")
            .description("Sets the SSL root certificate file.")
            .build();

    public static final PropertySpec LOGGER_LEVEL = PropertySpecModel.propertySpec()
            .name("loggerLevel")
            .description("Configures the logger level.")
            .build();

    public static final PropertySpec CHANNEL_BINDING = PropertySpecModel.propertySpec()
            .name("channelBinding")
            .description("Specifies the channel binding type.")
            .build();

    public static final PropertySpec LOGGER_FILE = PropertySpecModel.propertySpec()
            .name("loggerFile")
            .description("Sets the logger file location.")
            .build();

    public static final PropertySpec MAX_RESULT_BUFFER = PropertySpecModel.propertySpec()
            .name("maxResultBuffer")
            .description("Specifies the maximum result buffer size.")
            .build();

    public static final PropertySpec KERBEROS_SERVER_NAME = PropertySpecModel.propertySpec()
            .name("kerberosServerName")
            .description("Specifies the Kerberos server name.")
            .build();

    public static final PropertySpec USE_SPNEGO = BooleanPropertySpec.booleanPropertySpec()
            .name("useSpNego")
            .description("Specifies whether to use SPNEGO for authentication.")
            .build();

    public static final PropertySpec HIDE_UNPRIVILEGED_OBJECTS = BooleanPropertySpec.booleanPropertySpec()
            .name("hideUnprivilegedObjects")
            .description("Specifies whether to hide unprivileged objects.")
            .build();

    public static final PropertySpec XML_FACTORY_FACTORY = PropertySpecModel.propertySpec()
            .name("xmlFactoryFactory")
            .description("Specifies the XML factory class.")
            .build();

    public static final PropertySpec BINARY_TRANSFER_DISABLE = PropertySpecModel.propertySpec()
            .name("binaryTransferDisable")
            .description("Specifies the OIDs for which binary transfer should be disabled.")
            .build();

    public static final PropertySpec BINARY_TRANSFER_ENABLE = PropertySpecModel.propertySpec()
            .name("binaryTransferEnable")
            .description("Specifies the OIDs for which binary transfer should be enabled.")
            .build();

    public static final PropertySpec SSL_NEGOTIATION = PropertySpecModel.propertySpec()
            .name("sslNegotiation")
            .description("Configures the SSL negotiation method.")
            .build();

    public static final PropertySpec ASSUME_MIN_SERVER_VERSION = PropertySpecModel.propertySpec()
            .name("assumeMinServerVersion")
            .description("Specifies the assumed minimum server version.")
            .build();

    public static final PropertySpec CANCEL_SIGNAL_TIMEOUT = PropertySpecModel.propertySpec()
            .name("cancelSignalTimeout")
            .type(Integer.TYPE)
            .description("Specifies the cancel signal timeout.")
            .build();

    public static final List<? extends PropertySpec> PROPERTIES =
            Arrays.stream(PostgresDataSourceProperties.class.getDeclaredFields())
                    .filter(field -> Modifier.isPublic(field.getModifiers()) &&
                            Modifier.isStatic(field.getModifiers()) &&
                            Modifier.isFinal(field.getModifiers()) &&
                            !"PROPERTIES".equals(field.getName()))
                    .map(field -> {
                        try {
                            return Optional.of(field.get(null));
                        } catch (IllegalAccessException e) {
                            return Optional.empty();
                        }
                    }).filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(PropertySpec.class::isInstance)
                    .map(PropertySpec.class::cast)
                    .toList();

}
