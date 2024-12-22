package com.olsonsolution.common.data.domain.model.sql;

import com.olsonsolution.common.property.domain.model.BooleanPropertySpec;
import com.olsonsolution.common.property.domain.model.EnumPropertySpec;
import com.olsonsolution.common.property.domain.model.PropertySpecModel;
import com.olsonsolution.common.property.domain.port.stereotype.PropertySpec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.postgresql.jdbc.PreferQueryMode;

import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgresDataSourceProperties {

    public static final PropertySpec ADAPTIVE_FETCH = BooleanPropertySpec.booleanPropertySpec()
            .name("adaptiveFetch")
            .description("Enable or disable adaptive fetching.")
            .required(true)
            .build();

    public static final PropertySpec ADAPTIVE_FETCH_MINIMUM = PropertySpecModel.propertySpec()
            .name("adaptiveFetchMinimum")
            .description("Set the minimum adaptive fetch size.")
            .type(Integer.TYPE)
            .build();

    public static final PropertySpec ADAPTIVE_FETCH_MAXIMUM = PropertySpecModel.propertySpec()
            .name("adaptiveFetchMaximum")
            .description("Set the maximum adaptive fetch size.")
            .type(Integer.TYPE)
            .required(true)
            .build();

    public static final PropertySpec ALLOW_ENCODING_CHANGES = BooleanPropertySpec.booleanPropertySpec()
            .name("allowEncodingChanges")
            .description("Allow changes to client encoding settings.")
            .required(true)
            .build();

    public static final PropertySpec APPLICATION_NAME = PropertySpecModel.propertySpec()
            .name("applicationName")
            .description("Set the name of the application.")
            .build();

    public static final PropertySpec ASSUME_MIN_SERVER_VERSION = PropertySpecModel.propertySpec()
            .name("assumeMinServerVersion")
            .description("Assume the minimum server version for compatibility.")
            .build();

    public static final PropertySpec AUTHENTICATION_PLUGIN_CLASS_NAME = PropertySpecModel.propertySpec()
            .name("authenticationPluginClassName")
            .description("Specify the class name for the authentication plugin.")
            .build();

    public static final PropertySpec AUTOSAVE = PropertySpecModel.propertySpec()
            .name("autosave")
            .description("Set the autosave behavior for connections.")
            .build();

    public static final PropertySpec BINARY_TRANSFER = BooleanPropertySpec.booleanPropertySpec()
            .name("binaryTransfer")
            .description("Enable or disable binary transfer for data.")
            .build();

    public static final PropertySpec BINARY_TRANSFER_DISABLE = PropertySpecModel.propertySpec()
            .name("binaryTransferDisable")
            .description("Specify data types to disable binary transfer.")
            .build();

    public static final PropertySpec BINARY_TRANSFER_ENABLE = PropertySpecModel.propertySpec()
            .name("binaryTransferEnable")
            .description("Specify data types to enable binary transfer.")
            .build();

    public static final PropertySpec BINARY_TRANSFER_TIMEOUT = PropertySpecModel.propertySpec()
            .name("binaryTransferTimeout")
            .description("Set the timeout for binary transfers.")
            .type(Integer.TYPE)
            .required(true)
            .build();

    public static final PropertySpec CANCEL_SIGNAL_TIMEOUT = PropertySpecModel.propertySpec()
            .name("cancelSignalTimeout")
            .description("Set the timeout for cancel signals.")
            .type(Integer.TYPE)
            .required(true)
            .build();

    public static final PropertySpec CHANNEL_BINDING = PropertySpecModel.propertySpec()
            .name("channelBinding")
            .description("Specify the channel binding behavior.")
            .build();

    public static final PropertySpec CLEANUP_SAVE_POINTS = BooleanPropertySpec.booleanPropertySpec()
            .name("cleanupSavePoints")
            .description("Enable or disable cleanup of save points.")
            .build();

    public static final PropertySpec CONNECT_TIMEOUT = PropertySpecModel.propertySpec()
            .name("connectTimeout")
            .description("Set the timeout for establishing connections.")
            .type(Integer.TYPE)
            .required(true)
            .build();

    public static final PropertySpec DATABASE_METADATA_CACHE_FIELDS_SIZE = PropertySpecModel.propertySpec()
            .name("databaseMetadataCacheFieldsSize")
            .description("Set the cache size for database metadata fields.")
            .type(Integer.TYPE)
            .required(true)
            .build();

    public static final PropertySpec DEFAULT_RAW_FETCH_SIZE = PropertySpecModel.propertySpec()
            .name("defaultRawFetchSize")
            .description("Set the default fetch size for raw queries.")
            .type(Integer.TYPE)
            .required(true)
            .build();

    public static final PropertySpec DISABLE_COLUMN_SANITIZER = BooleanPropertySpec.booleanPropertySpec()
            .name("disableColumnSanitizer")
            .description("Enable or disable column name sanitization.")
            .required(true)
            .build();

    public static final PropertySpec ESCAPE_SYNTAX_CALL_MODE = PropertySpecModel.propertySpec()
            .name("escapeSyntaxCallMode")
            .description("Set the call mode for escape syntax.")
            .build();

    public static final PropertySpec GSS_ENC_MODE = PropertySpecModel.propertySpec()
            .name("gssEncMode")
            .description("Specify the GSS encryption mode.")
            .build();

    public static final PropertySpec GSS_RESPONSE_TIMEOUT = PropertySpecModel.propertySpec()
            .name("gssResponseTimeout")
            .description("Set the timeout for GSS responses.")
            .type(Integer.TYPE)
            .required(true)
            .build();

    public static final PropertySpec GSS_LIB = PropertySpecModel.propertySpec()
            .name("gssLib")
            .description("Specify the GSS library to use.")
            .build();

    public static final PropertySpec GROUP_STARTUP_PARAMETERS = PropertySpecModel.propertySpec()
            .name("groupStartupParameters")
            .description("Specify the group startup parameters.")
            .build();

    public static final PropertySpec HIDE_UNPRIVILEGED_OBJECTS = BooleanPropertySpec.booleanPropertySpec()
            .name("hideUnprivilegedObjects")
            .description("Enable or disable hiding of unprivileged objects.")
            .build();

    public static final PropertySpec HOST_RECHECK_SECONDS = PropertySpecModel.propertySpec()
            .name("hostRecheckSeconds")
            .description("Set the recheck interval for hosts in seconds.")
            .type(Integer.TYPE)
            .required(true)
            .build();

    public static final PropertySpec JAAS_APPLICATION_NAME = PropertySpecModel.propertySpec()
            .name("jaasApplicationName")
            .description("Specify the JAAS application name.")
            .build();

    public static final PropertySpec JAAS_LOGIN = BooleanPropertySpec.booleanPropertySpec()
            .name("jaasLogin")
            .description("Enable or disable JAAS login.")
            .required(true)
            .build();

    public static final PropertySpec KERBOS_SERVER_NAME = PropertySpecModel.propertySpec()
            .name("kerberosServerName")
            .description("Specify the Kerberos server name.")
            .build();

    public static final PropertySpec LOGIN_TIMEOUT = PropertySpecModel.propertySpec()
            .name("loginTimeout")
            .description("Set the timeout for login attempts.")
            .type(Integer.TYPE)
            .required(true)
            .build();

    public static final PropertySpec LOG_SERVER_ERROR_DETAIL = BooleanPropertySpec.booleanPropertySpec()
            .name("logServerErrorDetail")
            .description("Enable or disable logging of server error details.")
            .build();

    public static final PropertySpec LOG_UNCLOSED_CONNECTIONS = BooleanPropertySpec.booleanPropertySpec()
            .name("logUnclosedConnections")
            .description("Enable or disable logging of unclosed connections.")
            .build();

    public static final PropertySpec MAX_RESULT_BUFFER = PropertySpecModel.propertySpec()
            .name("maxResultBuffer")
            .description("Set the maximum result buffer size.")
            .type(Long.TYPE)
            .required(true)
            .build();

    public static final PropertySpec MAX_RESULT_BUFFER_SIZE = PropertySpecModel.propertySpec()
            .name("maxResultBufferSize")
            .description("Set the size of the maximum result buffer.")
            .type(Long.TYPE)
            .required(true)
            .build();

    public static final PropertySpec OPTIONS = PropertySpecModel.propertySpec()
            .name("options")
            .description("Specify additional options for the connection.")
            .build();

    public static final PropertySpec PREFER_QUERY_MODE = EnumPropertySpec.enumPropertySpec()
            .name("preferQueryMode")
            .description("Set the preferred query mode.")
            .type(PreferQueryMode.class)
            .build();

    public static final PropertySpec PREPARED_QUERY_CACHE_SIZE = PropertySpecModel.propertySpec()
            .name("preparedQueryCacheSize")
            .description("Set the size of the prepared query cache.")
            .type(Integer.TYPE)
            .required(true)
            .build();

    public static final PropertySpec PREPARED_TRASH_HOLD = PropertySpecModel.propertySpec()
            .name("preparedTrashHold")
            .description("Set the threshold for prepared query trash.")
            .type(Integer.TYPE)
            .required(true)
            .build();

    public static final PropertySpec QUOTE_RETURNING_IDENTIFIERS = BooleanPropertySpec.booleanPropertySpec()
            .name("quoteReturningIdentifiers")
            .description("Enable or disable quoting of returning identifiers.")
            .build();

    public static final PropertySpec RECEIVE_BUFFER_SIZE = PropertySpecModel.propertySpec()
            .name("receiveBufferSize")
            .description("Set the size of the receive buffer.")
            .type(Integer.TYPE)
            .required(true)
            .build();

    public static final PropertySpec RECV_BUFFER_SIZE = PropertySpecModel.propertySpec()
            .name("recvBufferSize")
            .description("Set the receive buffer size.")
            .type(Integer.TYPE)
            .required(true)
            .build();

    public static final PropertySpec REPLICATION = PropertySpecModel.propertySpec()
            .name("replication")
            .description("Specify the replication mode.")
            .build();

    public static final PropertySpec RE_WRITE_BATCH_INSERTS = BooleanPropertySpec.booleanPropertySpec()
            .name("reWriteBatchInserts")
            .description("Enable or disable rewriting of batch inserts.")
            .build();

    public static final PropertySpec SEND_BUFFER_SIZE = PropertySpecModel.propertySpec()
            .name("sendBufferSize")
            .description("Set the size of the send buffer.")
            .type(Integer.TYPE)
            .required(true)
            .build();

    public static final PropertySpec SOCKET_FACTORY = PropertySpecModel.propertySpec()
            .name("socketFactory")
            .description("Specify the socket factory class.")
            .build();

    public static final PropertySpec SOCKET_FACTORY_ARGS = PropertySpecModel.propertySpec()
            .name("socketFactoryArgs")
            .description("Specify arguments for the socket factory.")
            .build();

    public static final PropertySpec SOCKET_TIMEOUT = PropertySpecModel.propertySpec()
            .name("socketTimeout")
            .description("Set the timeout for socket operations.")
            .type(Integer.TYPE)
            .required(true)
            .build();

    public static final PropertySpec SSI_SERVER_CLASS = PropertySpecModel.propertySpec()
            .name("ssiServerClass")
            .description("Specify the SSI server class.")
            .build();

    public static final PropertySpec TARGET_SERVER_TYPE = PropertySpecModel.propertySpec()
            .name("targetServerType")
            .description("Specify the type of target server.")
            .build();

    public static final PropertySpec TCP_KEEP_ALIVE = BooleanPropertySpec.booleanPropertySpec()
            .name("tcpKeepAlive")
            .description("Enable or disable TCP keep-alive.")
            .build();

    public static final PropertySpec TCP_NO_DELAY = BooleanPropertySpec.booleanPropertySpec()
            .name("tcpNoDelay")
            .description("Enable or disable TCP no-delay.")
            .build();

    public static final PropertySpec UNKNOWN_LENGTH = PropertySpecModel.propertySpec()
            .name("unknownLength")
            .description("Set the default length for unknown fields.")
            .type(Long.TYPE)
            .required(true)
            .build();

    public static final PropertySpec USE_SP_NEGO = BooleanPropertySpec.booleanPropertySpec()
            .name("useSpNego")
            .description("Enable or disable SPNEGO authentication.")
            .build();

    public static final Set<PropertySpec> PROPERTIES_SPECIFICATIONS = Set.of(
            ADAPTIVE_FETCH,
            ADAPTIVE_FETCH_MINIMUM,
            ADAPTIVE_FETCH_MAXIMUM,
            ALLOW_ENCODING_CHANGES,
            APPLICATION_NAME,
            ASSUME_MIN_SERVER_VERSION,
            AUTHENTICATION_PLUGIN_CLASS_NAME,
            AUTOSAVE,
            BINARY_TRANSFER,
            BINARY_TRANSFER_DISABLE,
            BINARY_TRANSFER_ENABLE,
            BINARY_TRANSFER_TIMEOUT,
            CANCEL_SIGNAL_TIMEOUT,
            CHANNEL_BINDING,
            CLEANUP_SAVE_POINTS,
            CONNECT_TIMEOUT,
            DATABASE_METADATA_CACHE_FIELDS_SIZE,
            DEFAULT_RAW_FETCH_SIZE,
            DISABLE_COLUMN_SANITIZER,
            ESCAPE_SYNTAX_CALL_MODE,
            GSS_ENC_MODE,
            GSS_RESPONSE_TIMEOUT,
            GSS_LIB,
            GROUP_STARTUP_PARAMETERS,
            HIDE_UNPRIVILEGED_OBJECTS,
            HOST_RECHECK_SECONDS,
            JAAS_APPLICATION_NAME,
            JAAS_LOGIN,
            KERBOS_SERVER_NAME,
            LOGIN_TIMEOUT,
            LOG_SERVER_ERROR_DETAIL,
            LOG_UNCLOSED_CONNECTIONS,
            MAX_RESULT_BUFFER,
            MAX_RESULT_BUFFER_SIZE,
            OPTIONS,
            PREFER_QUERY_MODE,
            PREPARED_QUERY_CACHE_SIZE,
            PREPARED_TRASH_HOLD,
            QUOTE_RETURNING_IDENTIFIERS,
            RECEIVE_BUFFER_SIZE,
            RECV_BUFFER_SIZE,
            REPLICATION,
            RE_WRITE_BATCH_INSERTS,
            SEND_BUFFER_SIZE,
            SOCKET_FACTORY,
            SOCKET_FACTORY_ARGS,
            SOCKET_TIMEOUT,
            SSI_SERVER_CLASS,
            TARGET_SERVER_TYPE,
            TCP_KEEP_ALIVE,
            TCP_NO_DELAY,
            UNKNOWN_LENGTH,
            USE_SP_NEGO
    );

}
