package com.olsonsolution.common.data.domain.service.sql;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import com.olsonsolution.common.property.domain.port.stereotype.PropertySpec;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.olsonsolution.common.data.domain.model.sql.SqlPermissions.RO;
import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.POSTGRESQL;

@Slf4j
public class PostgresDataSourceModeler extends AbstractDataSourceModeler {

    private static final List<Map.Entry<PropertySpec, Method>> PROPERTY_SETTERS =
            AbstractDataSourceModeler.loadPropertySpecSetters(PGSimpleDataSource.class);

    private static final List<? extends PropertySpec> PROPERTIES = PROPERTY_SETTERS.stream()
            .map(Map.Entry::getKey)
            .toList();

    public PostgresDataSourceModeler() {
        super(POSTGRESQL, log, Collections.emptyList(), PROPERTIES);
    }

    @Override
    public DataSource create(SqlDataSource dataSource, SqlUser user, SqlPermission permission) {
        PGSimpleDataSource postgresDataSource = new PGSimpleDataSource();
        postgresDataSource.setServerNames(new String[]{dataSource.getHost()});
        Optional.ofNullable(dataSource.getPort()).map(port -> new int[]{port})
                .ifPresent(postgresDataSource::setPortNumbers);
        postgresDataSource.setUser(user.getUsername());
        postgresDataSource.setPassword(user.getPassword());
        if (permission.isSameAs(RO)) {
            postgresDataSource.setReadOnly(true);
        }
        loadProperties(postgresDataSource, dataSource, PROPERTY_SETTERS);
        return postgresDataSource;
    }
}
