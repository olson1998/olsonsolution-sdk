package com.olsonsolution.common.data.domain.service.sql;

import com.olsonsolution.common.data.domain.port.repository.sql.DataSourceModeler;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.property.domain.port.repository.PropertyReader;
import com.olsonsolution.common.property.domain.port.stereotype.PropertySpec;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Map.entry;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractDataSourceModeler implements DataSourceModeler {

    @Getter
    private final SqlVendor sqlVendor;

    private final Logger logger;

    private final List<PropertyReader<?>> propertyReaders;

    @Getter
    private final List<? extends PropertySpec> propertySpecifications;

    protected static Map<PropertySpec, Method> mapPropertyToSetter(Class<? extends DataSource> dataSourceClass,
                                                                   List<? extends PropertySpec> specifications) {
        return ReflectionUtils.reflectAllMethods(dataSourceClass)
                .filter(method -> StringUtils.startsWith(method.getName(), "set"))
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> Modifier.isPublic(method.getModifiers()) &&
                        !Modifier.isStatic(method.getModifiers()))
                .flatMap(method -> specifications.stream()
                        .filter(propertySpec -> isPropertySpecSetter(method, propertySpec))
                        .map(propertySpec -> entry(propertySpec, method)))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    protected final void loadProperties(DataSource dataSource,
                                        SqlDataSource sqlDataSource,
                                        Map<PropertySpec, Method> propertySetter) {
        Map<String, String> properties = sqlDataSource.getProperties();
        if (properties != null) {
            loadProperties(dataSource, properties, propertySetter);
        }
    }

    private static boolean isPropertySpecSetter(Method method,
                                                PropertySpec propertySpec) {
        String methodName = method.getName();
        if (method.getParameterCount() == 1 && StringUtils.startsWith(methodName, "set")) {
            String propertyName = propertySpec.getName();
            Class<?> propertyClass = propertySpec.getType();
            String methodPropName = StringUtils.substringAfter(methodName, "set");
            Type parameterType = method.getGenericParameterTypes()[0];
            return StringUtils.equalsIgnoreCase(propertyName, methodPropName) &&
                    propertyClass.equals(parameterType);
        } else {
            return false;
        }
    }

    private void loadProperties(DataSource dataSource,
                                Map<String, String> properties,
                                Map<PropertySpec, Method> propertySetter) {
        for (Map.Entry<String, String> propertyValue : properties.entrySet()) {
            String property = propertyValue.getKey();
            for (Map.Entry<PropertySpec, Method> propertySpecSetter : propertySetter.entrySet()) {
                PropertySpec propertySpec = propertySpecSetter.getKey();
                Method setter = propertySpecSetter.getValue();
                if (StringUtils.equals(property, propertySpec.getName())) {
                    String value = propertyValue.getValue();
                    loadProperty(dataSource, propertySpec, setter, value);
                }
            }
        }
    }

    private void loadProperty(DataSource dataSource,
                              PropertySpec propertySpec,
                              Method setter,
                              String propertyValue) {
        Class<?> propertyType = propertySpec.getType();
        Object value = propertyValue;
        if (!String.class.equals(propertyType)) {
            if (Boolean.TYPE.equals(propertyType)) {
                value = Boolean.parseBoolean(propertyValue);
            } else if (Integer.TYPE.equals(propertyType)) {
                value = Integer.parseInt(propertyValue);
            } else if (Long.TYPE.equals(propertyType)) {
                value = Long.parseLong(propertyValue);
            } else if (propertyType.isEnum()) {
                Class<? extends Enum> enumType = propertyType.asSubclass(Enum.class);
                value = Enum.valueOf(enumType, propertyValue);
            } else {
                Optional<PropertyReader<?>> propertyReader = findPropertyReader(propertyType);
                if (propertyReader.isPresent()) {
                    value = propertyReader.get().parse(propertyValue);
                } else {
                    logger.warn("Failed to read property, no property reader registered for: {}", propertyType);
                    return;
                }
            }
        }
        try {
            setter.invoke(dataSource, value);
        } catch (InvocationTargetException | IllegalAccessException e) {
            logger.error("Failed to set property, reason:", e);
        }
    }

    private Optional<PropertyReader<?>> findPropertyReader(Class<?> javaClass) {
        return Optional.ofNullable(propertyReaders)
                .stream()
                .flatMap(List::stream)
                .filter(propertyReader -> javaClass.isAssignableFrom(propertyReader.getClass()))
                .findFirst();
    }

}
