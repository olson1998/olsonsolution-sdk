package com.olsonsolution.common.reflection.domain.repository;

import java.lang.reflect.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReflectionUtils {

    boolean isAssignableFrom(Type targetType, Type pretenderType);

    List<Field> listFields(Class<?> javaClass, boolean includeSuperClasses);

    List<Method> listMethods(Class<?> javaClass, boolean includeSuperClasses);

    Optional<Field> findField(Class<?> javaClass, String fieldName, boolean includeSuperClass);

    Optional<Field> findField(Class<?> javaClass, Type returnedType, String fieldName, boolean includeSuperClasses);

    Optional<Method> findGetter(Field field);

    Optional<Method> findSetter(Field field);

    <T> Optional<Constructor<T>> findNoArgsConstructor(Class<T> javaClass);

    Map<TypeVariable<?>, Type> mapGenericsBounds(ParameterizedType parameterizedType);

}
