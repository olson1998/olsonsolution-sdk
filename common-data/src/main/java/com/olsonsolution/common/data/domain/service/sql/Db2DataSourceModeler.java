package com.olsonsolution.common.data.domain.service.sql;

import com.ibm.db2.jcc.DB2SimpleDataSource;
import com.olsonsolution.common.data.domain.model.sql.SqlVendors;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import com.olsonsolution.common.property.domain.port.stereotype.PropertySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.olsonsolution.common.data.domain.model.sql.SqlPermissions.RO;
import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.DB2;

@Slf4j
public class Db2DataSourceModeler extends AbstractDataSourceModeler {

    private static final String[] IGNORE_PROPERTIES = new String[]{
            "serverName",
            "portNumber",
            "databaseName",
            "user",
            "password",
            "readOnly"
    };

    private static final List<Map.Entry<PropertySpec, Method>> PROPERTY_SETTERS =
            AbstractDataSourceModeler.loadPropertySpecSetters(DB2SimpleDataSource.class).stream()
                    .filter(spec -> !isPreDefinedPropertySpecSetter(spec))
                    .toList();

    private static final List<? extends PropertySpec> PROPERTIES = PROPERTY_SETTERS.stream()
            .map(Map.Entry::getKey)
            .toList();

    public Db2DataSourceModeler() {
        super(DB2, log, Collections.emptyList(), PROPERTIES);
    }

    @Override
    public DataSource create(SqlDataSource dataSource, SqlUser user, SqlPermission permission) {
        DB2SimpleDataSource db2DataSource = new DB2SimpleDataSource();
        db2DataSource.setServerName(db2DataSource.getServerName());
        Optional.ofNullable(dataSource.getPort()).ifPresent(db2DataSource::setPortNumber);
        db2DataSource.setDatabaseName(db2DataSource.getDatabaseName());
        db2DataSource.setUser(user.getUsername());
        db2DataSource.setPassword(user.getPassword());
        if (permission.isSameAs(RO)) {
            db2DataSource.setReadOnly(true);
        }
        loadProperties(db2DataSource, dataSource, PROPERTY_SETTERS);
        return db2DataSource;
    }

    private static boolean isPreDefinedPropertySpecSetter(Map.Entry<PropertySpec, Method> propertySpecSetter) {
        PropertySpec propertySpec = propertySpecSetter.getKey();
        return StringUtils.equalsAny(propertySpec.getName(), IGNORE_PROPERTIES);
    }

}
