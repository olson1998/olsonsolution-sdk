package com.olsonsolution.common.spring.domain.service.datasource;

import com.olsonsolution.common.data.domain.model.sql.DomainSqlDataSource;
import com.olsonsolution.common.data.domain.model.sql.DomainSqlUser;
import com.olsonsolution.common.data.domain.model.sql.SqlPermissions;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.spring.domain.port.props.datasource.DestinationDataSourceProperties;
import com.olsonsolution.common.spring.domain.port.props.datasource.SqlDataSourceProperties;
import com.olsonsolution.common.spring.domain.port.props.datasource.SqlUsersProperties;
import com.olsonsolution.common.spring.domain.port.props.datasource.SqlVendorSupportProperties;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaSpecConfigurer;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.JpaDataSourceSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.olsonsolution.common.spring.domain.model.datasource.DomainJpaSpecDataSource.SYSTEM_JPA_SPEC;

@Slf4j
@RequiredArgsConstructor
public class DestinationDataSourcePropertyLookupService implements SqlDataSourceProvider {

    private final JpaSpecConfigurer jpaSpecConfigurer;

    private final SqlVendorSupportProperties sqlVendorSupportProperties;

    private final DestinationDataSourceProperties destinationDataSourceProperties;

    @Override
    public Optional<? extends SqlDataSource> findDestination(JpaDataSourceSpec jpaDataSourceSpec) {
        log.debug("Searching for destination datasource {}", jpaDataSourceSpec);
        String jpaSpec = jpaDataSourceSpec.getJpaSpec();
        String dsName = jpaDataSourceSpec.getDataSourceName();
        SqlPermission permission = jpaDataSourceSpec.getPermission();
        Optional<? extends SqlDataSourceProperties> sqlDataSourceProperties = destinationDataSourceProperties
                .getInstances()
                .stream()
                .filter(datasource -> isSameName(datasource, dsName))
                .findFirst();
        if (sqlDataSourceProperties.isPresent()) {
            Optional<String> matchingSchema = findSchema(sqlDataSourceProperties.get(), jpaSpec);
            if (matchingSchema.isEmpty()) {
                log.warn("SQL Data source '{}' JpaSpec: '{}' schema was not resovled", dsName, jpaSpec);
                return Optional.empty();
            }
            String schema = matchingSchema.get();
            Optional<? extends SqlUser> sqlUserProperties = sqlDataSourceProperties
                    .flatMap(ds -> findUserForSchema(ds, permission, schema));
            if (sqlUserProperties.isEmpty()) {
                log.warn("SQL Data source '{}' JpaSpec: '{}' schema: '{}' with permission: '{}'" +
                        " configured in properties but users not found", dsName, jpaSpec, schema, permission
                );
            } else {
                SqlUser sqlUser = getUser(sqlUserProperties.get(), dsName, schema);
                return Optional.of(buildSqlDataSource(sqlDataSourceProperties.get(), sqlUser, schema));
            }
        } else {
            log.warn("SQL Data source '{}' JpaSpec: '{}' not configured in properties", dsName, jpaSpec);
        }
        return Optional.empty();
    }

    private SqlDataSource buildSqlDataSource(SqlDataSourceProperties properties, SqlUser user, String schema) {
        SqlVendor vendor = properties.getVendor();
        Properties additionalPropertiesObj = properties.getProperty();
        Map<String, String> additionalProperties = additionalPropertiesObj.keySet()
                .stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(property -> new DefaultMapEntry<>(property, additionalPropertiesObj.getProperty(property)))
                .collect(Collectors.toUnmodifiableMap(DefaultMapEntry::getKey, DefaultMapEntry::getValue));
        DomainSqlDataSource.DomainSqlDataSourceBuilder sqlDataSource = DomainSqlDataSource.builder()
                .vendor(vendor)
                .host(properties.getHost())
                .port(properties.getPort())
                .user(user)
                .properties(additionalProperties);
        if (vendor.isSupportSchemas()) {
            sqlDataSource.database(properties.getDatabase())
                    .schema(schema);
        } else {
            sqlDataSource.database(schema);
        }
        return sqlDataSource.build();
    }

    private SqlUser getUser(SqlUser sqlUser, String dataSourceName, String schema) {
        String username = sqlUser.getUsername();
        String password = sqlUser.getPassword();
        if (StringUtils.isNoneBlank(username, password)) {
            return new DomainSqlUser(username, password);
        }
        throw new IllegalArgumentException(
                "SQL user for data source: '%s' schema: '%s' must have username and password"
                        .formatted(dataSourceName, schema)
        );
    }

    private Optional<? extends SqlUser> findUserForSchema(SqlDataSourceProperties properties,
                                                          SqlPermission permission,
                                                          String schema) {
        return properties.getUsers()
                .stream()
                .filter(user -> StringUtils.equals(user.getSchema(), schema))
                .findFirst()
                .flatMap(users -> findUserWithPermission(users, permission));
    }

    private Optional<? extends SqlUser> findUserWithPermission(SqlUsersProperties usersProperties,
                                                               SqlPermission permission) {
        SqlUser sqlUser = null;
        if (permission.isSameAs(SqlPermissions.RO)) {
            sqlUser = usersProperties.getReadOnly();
        } else if (permission.isSameAs(SqlPermissions.WO)) {
            sqlUser = usersProperties.getWriteOnly();
        } else if (permission.isSameAs(SqlPermissions.RW)) {
            sqlUser = usersProperties.getReadWrite();
        } else if (permission.isSameAs(SqlPermissions.RWX)) {
            sqlUser = usersProperties.getReadWriteExecute();
        }
        return Optional.ofNullable(sqlUser);
    }

    private Optional<String> findSchema(SqlDataSourceProperties properties, String jpaSpec) {
        SqlVendor vendor = properties.getVendor();
        if (StringUtils.equals(jpaSpec, SYSTEM_JPA_SPEC)) {
            return sqlVendorSupportProperties.getVendorDefaults()
                    .stream()
                    .filter(defaultsProps -> vendor.isSameAs(defaultsProps.getVendorName()))
                    .findFirst()
                    .map(defaultsProps -> {
                        if (vendor.isSupportSchemas()) {
                            return defaultsProps.getSchema();
                        } else {
                            return defaultsProps.getCatalog();
                        }
                    });
        } else {
            return Optional.ofNullable(jpaSpecConfigurer.resolveSchema(jpaSpec));
        }
    }

    private boolean isSameName(SqlDataSourceProperties properties, String dataSourceName) {
        return StringUtils.equals(properties.getName(), dataSourceName);
    }

}
