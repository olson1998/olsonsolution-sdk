package com.olsonsolution.common.data.domain.service.sql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ReflectionUtils {

    static Stream<Method> reflectAllMethods(Class<?> javaClass) {
        Stream.Builder<Method> methods = Stream.builder();
        reflectMethods(javaClass, methods);
        return methods.build();
    }

    private static void reflectMethods(Class<?> javaClass, Stream.Builder<Method> methods) {
        if(!Object.class.equals(javaClass)) {
            Arrays.stream(javaClass.getDeclaredMethods()).forEach(methods::add);
            if(javaClass.getSuperclass() != null) {
                reflectMethods(javaClass.getSuperclass(), methods);
            }
        }
    }

}
