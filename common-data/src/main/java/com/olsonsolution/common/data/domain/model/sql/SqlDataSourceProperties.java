package com.olsonsolution.common.data.domain.model.sql;

import com.olsonsolution.common.property.domain.model.BooleanPropertySpec;
import com.olsonsolution.common.property.domain.model.PropertySpecModel;
import com.olsonsolution.common.property.domain.port.stereotype.PropertySpec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.ietf.jgss.GSSCredential;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SqlDataSourceProperties {

    public static final PropertySpec LOGIN_TIMEOUT = PropertySpecModel.propertySpec()
            .name("loginTimeout")
            .type(int.class)
            .description("Maximum time to wait for a connection in seconds.")
            .build();

    public static final PropertySpec APPLICATION_NAME = PropertySpecModel.propertySpec()
            .name("applicationName")
            .description("Name of the application connecting to the server.")
            .build();

    public static final PropertySpec INSTANCE_NAME = PropertySpecModel.propertySpec()
            .name("instanceName")
            .description("Name of the SQL Server instance to connect to.")
            .build();

    public static final PropertySpec INTEGRATED_SECURITY = BooleanPropertySpec.booleanPropertySpec()
            .name("integratedSecurity")
            .description("Enable integrated security for SQL Server connection.")
            .build();

    public static final PropertySpec AUTHENTICATION_SCHEME = PropertySpecModel.propertySpec()
            .name("authenticationScheme")
            .description("Specifies the authentication scheme.")
            .build();

    public static final PropertySpec AUTHENTICATION = PropertySpecModel.propertySpec()
            .name("authentication")
            .description("Specifies the authentication method.")
            .build();

    public static final PropertySpec GSS_CREDENTIALS = PropertySpecModel.propertySpec()
            .name("gssCredentials")
            .type(GSSCredential.class)
            .description("Specifies GSS credentials for authentication.")
            .build();

    public static final PropertySpec USE_DEFAULT_GSS_CREDENTIAL = BooleanPropertySpec.booleanPropertySpec()
            .name("useDefaultGSSCredential")
            .description("Use the default GSS credential.")
            .build();

    public static final PropertySpec ACCESS_TOKEN = PropertySpecModel.propertySpec()
            .name("accessToken")
            .description("Access token for authentication.")
            .build();

    public static final PropertySpec COLUMN_ENCRYPTION_SETTING = PropertySpecModel.propertySpec()
            .name("columnEncryptionSetting")
            .description("Specifies the column encryption setting.")
            .build();

    public static final PropertySpec KEY_STORE_AUTHENTICATION = PropertySpecModel.propertySpec()
            .name("keyStoreAuthentication")
            .description("Specifies the key store authentication method.")
            .build();

    public static final PropertySpec KEY_STORE_SECRET = PropertySpecModel.propertySpec()
            .name("keyStoreSecret")
            .description("Specifies the key store secret.")
            .build();

    public static final PropertySpec KEY_STORE_LOCATION = PropertySpecModel.propertySpec()
            .name("keyStoreLocation")
            .description("Specifies the key store location.")
            .build();

    public static final PropertySpec LAST_UPDATE_COUNT = BooleanPropertySpec.booleanPropertySpec()
            .name("lastUpdateCount")
            .description("Return only the last update count in a batch.")
            .build();

    public static final PropertySpec ENCRYPT = PropertySpecModel.propertySpec()
            .name("encrypt")
            .description("Specifies the encryption setting.")
            .build();

    public static final PropertySpec SERVER_CERTIFICATE = PropertySpecModel.propertySpec()
            .name("serverCertificate")
            .description("Specifies the server certificate.")
            .build();

    public static final PropertySpec TRANSPARENT_NETWORK_IP_RESOLUTION = BooleanPropertySpec.booleanPropertySpec()
            .name("transparentNetworkIPResolution")
            .description("Enable transparent network IP resolution.")
            .build();

    public static final PropertySpec TRUST_SERVER_CERTIFICATE = BooleanPropertySpec.booleanPropertySpec()
            .name("trustServerCertificate")
            .description("Trust the server certificate.")
            .required(true)
            .build();

    public static final PropertySpec TRUST_STORE_TYPE = PropertySpecModel.propertySpec()
            .name("trustStoreType")
            .description("Specifies the trust store type.")
            .build();

    public static final PropertySpec TRUST_STORE = PropertySpecModel.propertySpec()
            .name("trustStore")
            .description("Specifies the trust store location.")
            .build();

    public static final PropertySpec TRUST_STORE_PASSWORD = PropertySpecModel.propertySpec()
            .name("trustStorePassword")
            .description("Specifies the trust store password.")
            .build();

    public static final PropertySpec HOST_NAME_IN_CERTIFICATE = PropertySpecModel.propertySpec()
            .name("hostNameInCertificate")
            .description("Specifies the host name in the certificate.")
            .build();

    public static final PropertySpec LOCK_TIMEOUT = PropertySpecModel.propertySpec()
            .name("lockTimeout")
            .type(int.class)
            .description("Specifies the lock timeout in milliseconds.")
            .build();

    public static final PropertySpec SELECT_METHOD = PropertySpecModel.propertySpec()
            .name("selectMethod")
            .description("Specifies the select method.")
            .build();

    public static final PropertySpec RESPONSE_BUFFERING = PropertySpecModel.propertySpec()
            .name("responseBuffering")
            .description("Specifies the response buffering setting.")
            .build();

    public static final PropertySpec APPLICATION_INTENT = PropertySpecModel.propertySpec()
            .name("applicationIntent")
            .description("Specifies the application intent.")
            .build();

    public static final PropertySpec REPLICATION = BooleanPropertySpec.booleanPropertySpec()
            .name("replication")
            .description("Enable replication support.")
            .required(true)
            .build();

    public static final PropertySpec SEND_TIME_AS_DATETIME = BooleanPropertySpec.booleanPropertySpec()
            .name("sendTimeAsDatetime")
            .description("Send time values as datetime.")
            .required(true)
            .build();

    public static final PropertySpec DATETIME_PARAMETER_TYPE = PropertySpecModel.propertySpec()
            .name("datetimeParameterType")
            .description("Specifies the datetime parameter type.")
            .build();

    public static final PropertySpec USE_FMT_ONLY = BooleanPropertySpec.booleanPropertySpec()
            .name("useFmtOnly")
            .description("Enable useFmtOnly setting.")
            .build();

    public static final PropertySpec DELAY_LOADING_LOBS = BooleanPropertySpec.booleanPropertySpec()
            .name("delayLoadingLobs")
            .description("Delay loading of LOBs.")
            .build();

    public static final PropertySpec SEND_STRING_PARAMETERS_AS_UNICODE = BooleanPropertySpec.booleanPropertySpec()
            .name("sendStringParametersAsUnicode")
            .description("Send string parameters as Unicode.")
            .required(true)
            .build();

    public static final PropertySpec SERVER_NAME_AS_ACE = BooleanPropertySpec.booleanPropertySpec()
            .name("serverNameAsACE")
            .description("Enable server name as ACE.")
            .required(true)
            .build();

    public static final PropertySpec IP_ADDRESS_PREFERENCE = PropertySpecModel.propertySpec()
            .name("ipAddressPreference")
            .type(String.class)
            .description("Specifies the IP address preference.")
            .required(false)
            .build();

    public static final PropertySpec REALM = PropertySpecModel.propertySpec()
            .name("realm")
            .type(String.class)
            .description("Specifies the Kerberos realm.")
            .required(false)
            .build();

    public static final PropertySpec SERVER_SPN = PropertySpecModel.propertySpec()
            .name("serverSpn")
            .type(String.class)
            .description("Specifies the server SPN.")
            .required(false)
            .build();

    public static final PropertySpec FAILOVER_PARTNER = PropertySpecModel.propertySpec()
            .name("failoverPartner")
            .type(String.class)
            .description("Specifies the failover partner server name.")
            .required(false)
            .build();

    public static final PropertySpec MULTI_SUBNET_FAILOVER = BooleanPropertySpec.booleanPropertySpec()
            .name("multiSubnetFailover")
            .description("Enable multi-subnet failover.")
            .required(true)
            .build();

    public static final PropertySpec WORKSTATION_ID = PropertySpecModel.propertySpec()
            .name("workstationID")
            .type(String.class)
            .description("Specifies the workstation ID.")
            .required(false)
            .build();

    public static final PropertySpec XOPEN_STATES = BooleanPropertySpec.booleanPropertySpec()
            .name("xopenStates")
            .description("Enable XOPEN state conversion.")
            .required(true)
            .build();

    public static final PropertySpec FIPS = BooleanPropertySpec.booleanPropertySpec()
            .name("fips")
            .description("Enable FIPS compliance.")
            .required(true)
            .build();

    public static final PropertySpec SOCKET_FACTORY_CLASS = PropertySpecModel.propertySpec()
            .name("socketFactoryClass")
            .description("Specifies the socket factory class.")
            .build();

    public static final PropertySpec SOCKET_FACTORY_CONSTRUCTOR_ARG = PropertySpecModel.propertySpec()
            .name("socketFactoryConstructorArg")
            .description("Specifies the socket factory constructor argument.")
            .build();

    public static final PropertySpec SSL_PROTOCOL = PropertySpecModel.propertySpec()
            .name("sslProtocol")
            .description("Specifies the SSL protocol.")
            .build();

    public static final PropertySpec TRUST_MANAGER_CLASS = PropertySpecModel.propertySpec()
            .name("trustManagerClass")
            .description("Specifies the trust manager class.")
            .build();

    public static final PropertySpec TRUST_MANAGER_CONSTRUCTOR_ARG = PropertySpecModel.propertySpec()
            .name("trustManagerConstructorArg")
            .description("Specifies the trust manager constructor argument.")
            .build();

    public static final PropertySpec DESCRIPTION = PropertySpecModel.propertySpec()
            .name("description")
            .description("Specifies the datasource description.")
            .build();

    public static final PropertySpec PACKET_SIZE = PropertySpecModel.propertySpec()
            .name("packetSize")
            .type(int.class)
            .description("Specifies the TCP/IP packet size.")
            .build();

    public static final PropertySpec QUERY_TIMEOUT = PropertySpecModel.propertySpec()
            .name("queryTimeout")
            .type(int.class)
            .description("Specifies the query timeout in seconds.")
            .build();

    public static final PropertySpec CANCEL_QUERY_TIMEOUT = PropertySpecModel.propertySpec()
            .name("cancelQueryTimeout")
            .type(int.class)
            .description("Specifies the cancel query timeout in seconds.")
            .build();

    public static final PropertySpec ENABLE_PREPARE_ON_FIRST_PREPARED_STATEMENT_CALL = BooleanPropertySpec.booleanPropertySpec()
            .name("enablePrepareOnFirstPreparedStatementCall")
            .description("Enable prepare on first prepared statement call.")
            .required(true)
            .build();

    public static final PropertySpec CACHE_BULK_COPY_METADATA = BooleanPropertySpec.booleanPropertySpec()
            .name("cacheBulkCopyMetadata")
            .description("Enable caching of bulk copy metadata.")
            .required(true)
            .build();

    public static final PropertySpec SERVER_PREPARED_STATEMENT_DISCARD_THRESHOLD = PropertySpecModel.propertySpec()
            .name("serverPreparedStatementDiscardThreshold")
            .type(int.class)
            .description("Specifies the prepared statement discard threshold.")
            .build();

    public static final PropertySpec STATEMENT_POOLING_CACHE_SIZE = PropertySpecModel.propertySpec()
            .name("statementPoolingCacheSize")
            .type(int.class)
            .description("Specifies the statement pooling cache size.")
            .build();

    public static final PropertySpec DISABLE_STATEMENT_POOLING = BooleanPropertySpec.booleanPropertySpec()
            .name("disableStatementPooling")
            .description("Disable statement pooling.")
            .required(true)
            .build();

    public static final PropertySpec SOCKET_TIMEOUT = PropertySpecModel.propertySpec()
            .name("socketTimeout")
            .type(int.class)
            .description("Specifies the socket timeout in milliseconds.")
            .build();

    public static final PropertySpec USE_BULK_COPY_FOR_BATCH_INSERT = BooleanPropertySpec.booleanPropertySpec()
            .name("useBulkCopyForBatchInsert")
            .description("Enable use of bulk copy for batch insert.")
            .required(true)
            .build();

    public static final PropertySpec JAAS_CONFIGURATION_NAME = PropertySpecModel.propertySpec()
            .name("jaasConfigurationName")
            .description("Specifies the JAAS configuration name.")
            .build();


    public static final PropertySpec KEY_VAULT_PROVIDER_CLIENT_ID = PropertySpecModel.propertySpec()
            .name("keyVaultProviderClientId")
            .description("Specifies the Key Vault provider client ID.")
            .build();

    public static final PropertySpec KEY_VAULT_PROVIDER_CLIENT_KEY = PropertySpecModel.propertySpec()
            .name("keyVaultProviderClientKey")
            .description("Specifies the Key Vault provider client key.")
            .build();

    public static final PropertySpec KEY_STORE_PRINCIPAL_ID = PropertySpecModel.propertySpec()
            .name("keyStorePrincipalId")
            .description("Specifies the Key Store principal ID.")
            .build();

    public static final PropertySpec DOMAIN = PropertySpecModel.propertySpec()
            .name("domain")
            .description("Specifies the domain.")
            .build();

    public static final PropertySpec ENCLAVE_ATTESTATION_URL = PropertySpecModel.propertySpec()
            .name("enclaveAttestationUrl")
            .description("Specifies the enclave attestation URL.")
            .build();

    public static final PropertySpec ENCLAVE_ATTESTATION_PROTOCOL = PropertySpecModel.propertySpec()
            .name("enclaveAttestationProtocol")
            .description("Specifies the enclave attestation protocol.")
            .build();

    public static final PropertySpec CLIENT_CERTIFICATE = PropertySpecModel.propertySpec()
            .name("clientCertificate")
            .description("Specifies the path to the client certificate.")
            .build();

    public static final PropertySpec CLIENT_KEY = PropertySpecModel.propertySpec()
            .name("clientKey")
            .description("Specifies the path to the client key.")
            .build();

    public static final PropertySpec CLIENT_KEY_PASSWORD = PropertySpecModel.propertySpec()
            .name("clientKeyPassword")
            .description("Specifies the client key password.")
            .build();

    public static final PropertySpec SEND_TEMPORAL_DATA_TYPES_AS_STRING_FOR_BULK_COPY = BooleanPropertySpec.booleanPropertySpec()
            .name("sendTemporalDataTypesAsStringForBulkCopy")
            .description("Send temporal data types as string for bulk copy.")
            .required(true)
            .build();

    public static final PropertySpec MAX_RESULT_BUFFER = PropertySpecModel.propertySpec()
            .name("maxResultBuffer")
            .description("Specifies the maximum result buffer.")
            .build();

    public static final PropertySpec CONNECT_RETRY_COUNT = PropertySpecModel.propertySpec()
            .name("connectRetryCount")
            .description("Specifies the connection retry count.")
            .build();

    public static final PropertySpec CONNECT_RETRY_INTERVAL = PropertySpecModel.propertySpec()
            .name("connectRetryInterval")
            .description("Specifies the connection retry interval.")
            .build();

    public static final PropertySpec PREPARE_METHOD = PropertySpecModel.propertySpec()
            .name("prepareMethod")
            .description("Specifies the prepare method.")
            .build();

    public static final PropertySpec CALC_BIG_DECIMAL_PRECISION = BooleanPropertySpec.booleanPropertySpec()
            .name("calcBigDecimalPrecision")
            .description("Calculate BigDecimal precision.")
            .required(true)
            .build();

    public static final PropertySpec ACCESS_TOKEN_CALLBACK = PropertySpecModel.propertySpec()
            .name("accessTokenCallback")
            .description("Specifies the access token callback.")
            .build();

    public static final PropertySpec ACCESS_TOKEN_CALLBACK_CLASS = PropertySpecModel.propertySpec()
            .name("accessTokenCallbackClass")
            .description("Specifies the access token callback class.")
            .build();

    public static final List<? extends PropertySpec> PROPERTIES =
            Arrays.stream(SqlDataSourceProperties.class.getDeclaredFields())
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

