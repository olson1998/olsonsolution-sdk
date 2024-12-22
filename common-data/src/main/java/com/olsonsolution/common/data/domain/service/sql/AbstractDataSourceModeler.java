package com.olsonsolution.common.data.domain.service.sql;

import com.olsonsolution.common.data.domain.port.repository.sql.DataSourceModeler;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.property.domain.model.BooleanPropertySpec;
import com.olsonsolution.common.property.domain.model.EnumPropertySpec;
import com.olsonsolution.common.property.domain.model.PropertySpecModel;
import com.olsonsolution.common.property.domain.port.repository.PropertyReader;
import com.olsonsolution.common.property.domain.port.stereotype.PropertySpec;
import com.olsonsolution.common.reflection.domain.service.ReflectionUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractDataSourceModeler implements DataSourceModeler {

    @Getter
    private final SqlVendor sqlVendor;

    private final Logger logger;

    private final List<PropertyReader<?>> propertyReaders;

    @Getter
    private final Collection<? extends PropertySpec> propertySpecifications;

    protected final void loadProperties(DataSource dataSource,
                                        SqlDataSource sqlDataSource,
                                        List<Map.Entry<PropertySpec, Method>> propertySetter) {
        Map<String, String> properties = sqlDataSource.getProperties();
        if (properties != null) {
            loadProperties(dataSource, properties, propertySetter);
        }
    }

    protected static List<Map.Entry<PropertySpec, Method>> loadPropertySpecSetters(@NonNull
                                                                                 Class<? extends DataSource> dsc) {
        return ReflectionUtils.listSetters(dsc, true)
                .stream()
                .filter(method -> !method.isAnnotationPresent(Deprecated.class))
                .map(method -> entry(createPropertySpec(method), method))
                .toList();
    }

    protected static List<? extends PropertySpec> loadPropertySpec(@NonNull Class<? extends DataSource> dsc) {
        return ReflectionUtils.listSetters(dsc, true)
                .stream()
                .filter(method -> !method.isAnnotationPresent(Deprecated.class))
                .map(AbstractDataSourceModeler::createPropertySpec)
                .toList();
    }

    private static PropertySpec createPropertySpec(@NonNull Method method) {
        String property = StringUtils.substringAfter(method.getName(), "set");
        if(!StringUtils.isAllUpperCase(property)) {
            property = StringUtils.uncapitalize(property);
        }
        Type propertyType = method.getGenericParameterTypes()[0];
        if (propertyType instanceof Class<?> javaClass &&
                (Boolean.TYPE.equals(javaClass) || Boolean.class.equals(javaClass))) {
            BooleanPropertySpec.BooleanPropertySpecBuilder builder = BooleanPropertySpec.booleanPropertySpec();
            if (Boolean.TYPE.equals(javaClass)) {
                builder.required(true);
            } else {
                builder.required(false);
            }
            return builder .name(property)
                    .description(StringUtils.EMPTY)
                    .build();
        } else if (propertyType instanceof Class<?> javaClass && javaClass.isEnum()) {
            Class<? extends Enum> enumClass = javaClass.asSubclass(Enum.class);
            return EnumPropertySpec.enumPropertySpec()
                    .name(property)
                    .type(enumClass)
                    .description(StringUtils.EMPTY)
                    .build();
        } else {
            return PropertySpecModel.propertySpec()
                    .name(property)
                    .type(propertyType)
                    .description(StringUtils.EMPTY)
                    .build();
        }
    }

    private void loadProperties(DataSource dataSource,
                                Map<String, String> properties,
                                List<Map.Entry<PropertySpec, Method>> propertySetter) {
        for (Map.Entry<String, String> propertyValue : properties.entrySet()) {
            String property = propertyValue.getKey();
            for (Map.Entry<PropertySpec, Method> propertySpecSetter : propertySetter) {
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
        Type propertyType = propertySpec.getType();
        Object value = propertyValue;
        if (!String.class.equals(propertyType)) {
            if (Boolean.TYPE.equals(propertyType)) {
                value = Boolean.parseBoolean(propertyValue);
            } else if (Integer.TYPE.equals(propertyType)) {
                value = Integer.parseInt(propertyValue);
            } else if (Long.TYPE.equals(propertyType)) {
                value = Long.parseLong(propertyValue);
            } else if (propertyType instanceof Class<?> javaClass && javaClass.isEnum()) {
                Class<? extends Enum> enumType = javaClass.asSubclass(Enum.class);
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

    private Optional<PropertyReader<?>> findPropertyReader(Type type) {
        return Optional.ofNullable(propertyReaders)
                .stream()
                .flatMap(List::stream)
                .filter(propertyReader -> type.equals(propertyReader.getPropertyType()))
                .findFirst();
    }

}
