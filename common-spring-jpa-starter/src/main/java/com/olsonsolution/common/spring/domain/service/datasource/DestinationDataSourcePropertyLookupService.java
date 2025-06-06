package com.olsonsolution.common.spring.domain.service.datasource;

import com.olsonsolution.common.data.domain.model.sql.DomainSqlDataSource;
import com.olsonsolution.common.data.domain.model.sql.SqlPermissions;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.spring.domain.port.props.datasource.DestinationDataSourceProperties;
import com.olsonsolution.common.spring.domain.port.props.datasource.SqlDataSourceProperties;
import com.olsonsolution.common.spring.domain.port.props.datasource.SqlUsersProperties;
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

@Slf4j
@RequiredArgsConstructor
public class DestinationDataSourcePropertyLookupService implements SqlDataSourceProvider {

    private final JpaSpecConfigurer jpaSpecConfigurer;

    private final DestinationDataSourceProperties destinationDataSourceProperties;

    @Override
    public Optional<? extends SqlDataSource> findDestination(JpaDataSourceSpec jpaDataSourceSpec) {
        log.debug("Searching for destination datasource {}", jpaDataSourceSpec);
        String jpaSpec = jpaDataSourceSpec.getJpaSpec();
        String dsName = jpaDataSourceSpec.getDataSourceName();
        String schema = jpaSpecConfigurer.resolveSchema(jpaSpec);
        SqlPermission permission = jpaDataSourceSpec.getPermission();
        Optional<? extends SqlDataSourceProperties> sqlDataSourceProperties = destinationDataSourceProperties
                .getInstance()
                .stream()
                .filter(datasource -> isSameName(datasource, dsName))
                .findFirst();
        if (sqlDataSourceProperties.isPresent()) {
            Optional<? extends SqlUser> user = sqlDataSourceProperties
                    .flatMap(ds -> findUserForSchema(ds, permission, schema));
            if (user.isEmpty()) {
                log.warn("SQL Data source '{}' JpaSpec: '{}' schema: '{}' with permission: '{}'" +
                        " configured in properties but users not found", dsName, jpaSpec, schema, permission
                );
            } else {
                return Optional.of(buildSqlDataSource(sqlDataSourceProperties.get(), user.get(), schema));
            }
        } else {
            log.warn(
                    "SQL Data source '{}' JpaSpec: '{}' schema: '{}' not configured in properties",
                    dsName, jpaSpec, schema
            );
        }
        return Optional.empty();
    }

    private SqlDataSource buildSqlDataSource(SqlDataSourceProperties properties, SqlUser user, String schema) {
        SqlVendor vendor = properties.getVendor();
        Properties additionalPropertiesObj = properties.getProperties();
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

    private Optional<? extends SqlUser> findUserForSchema(SqlDataSourceProperties properties,
                                                          SqlPermission permission,
                                                          String schema) {
        return properties.getUser()
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
            sqlUser = usersProperties.getReadWrite();
        }
        return Optional.ofNullable(sqlUser);
    }

    private boolean isSameName(SqlDataSourceProperties properties, String dataSourceName) {
        return StringUtils.equals(properties.getName(), dataSourceName);
    }

}
