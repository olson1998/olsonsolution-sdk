package com.olsonsolution.common.data.domain.service.sql;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import com.olsonsolution.common.property.domain.port.stereotype.PropertySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.olsonsolution.common.data.domain.model.sql.PostgresDataSourceProperties.PROPERTIES_SPECIFICATIONS;
import static com.olsonsolution.common.data.domain.model.sql.SqlPermissions.RO;
import static com.olsonsolution.common.data.domain.model.sql.SqlVendors.POSTGRESQL;
import static java.util.Map.entry;

@Slf4j
public class PostgresDataSourceModeler extends AbstractDataSourceModeler {

    private static final Map<PropertySpec, Method> PROPERTY_METHOD_SETTER = mapPropertySpecToSetter();

    public PostgresDataSourceModeler() {
        super(POSTGRESQL, log);
    }

    @Override
    public DataSource create(SqlDataSource dataSource, SqlUser user, SqlPermission permission) {
        PGSimpleDataSource postgresDataSource = new PGSimpleDataSource();
        postgresDataSource.setServerNames(new String[]{dataSource.getHost()});
        Optional.ofNullable(dataSource.getPort()).map(p -> new int[]{p})
                .ifPresent(postgresDataSource::setPortNumbers);
        postgresDataSource.setDatabaseName(dataSource.getDatabase());
        postgresDataSource.setUser(user.getUsername());
        postgresDataSource.setPassword(user.getPassword());
        if (permission.isSameAs(RO)) {
            postgresDataSource.setReadOnly(true);
        }
        loadProperties(postgresDataSource, dataSource, PROPERTY_METHOD_SETTER);
        return postgresDataSource;
    }

    private static Map<PropertySpec, Method> mapPropertySpecToSetter() {
        return ReflectionUtils.reflectAllMethods(PGSimpleDataSource.class)
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        PostgresDataSourceModeler::mapPropertySpecToSetter
                ));
    }

    private static Map<PropertySpec, Method> mapPropertySpecToSetter(List<Method> setters) {
        Stream.Builder<Map.Entry<PropertySpec, Method>> propertySpecSetters = Stream.builder();
        PROPERTIES_SPECIFICATIONS.forEach(propertySpec -> setters.stream()
                .filter(method -> isPropertySpecSetter(propertySpec, method))
                .findFirst()
                .ifPresent(method -> propertySpecSetters.add(entry(propertySpec, method))));
        return propertySpecSetters.build()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static boolean isPropertySpecSetter(PropertySpec propertySpec, Method method) {
        String setterName = resolveSetterName(propertySpec);
        Class<?> setterType = propertySpec.getType();
        return StringUtils.equals(method.getName(), setterName) && method.getParameterCount() == 1 &&
                method.getParameters()[0].getType().equals(setterType);
    }

    private static String resolveSetterName(PropertySpec propertySpec) {
        return "set" + StringUtils.capitalize(propertySpec.getName());
    }

}
