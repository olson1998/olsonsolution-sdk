package com.olsonsolution.common.reflection.domain.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Map.entry;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtils {

    public static boolean isAssignableFrom(Type targetType, Type pretenderType) {
        if (targetType instanceof Class<?> targetClass && pretenderType instanceof Class<?> pretenderClass) {
            return pretenderClass.isAssignableFrom(targetClass);
        } else if (targetType instanceof ParameterizedType targetParamType &&
                pretenderType instanceof Class<?> pretenderClass) {
            if (targetParamType.getRawType() instanceof Class<?> targetRawClass) {
                return pretenderClass.isAssignableFrom(targetRawClass);
            }
        } else if (targetType instanceof ParameterizedType targetParamType &&
                pretenderType instanceof ParameterizedType pretenderParamType) {
            if (targetParamType.getRawType() instanceof Class<?> targetRawClass &&
                    pretenderParamType.getRawType() instanceof Class<?> pretenderRawClass) {
                if (pretenderRawClass.isAssignableFrom(targetRawClass)) {
                    Type[] targetGenerics = targetParamType.getActualTypeArguments();
                    Type[] pretenderGenerics = pretenderParamType.getActualTypeArguments();
                    if (targetGenerics.length == pretenderGenerics.length) {
                        boolean[] matchers = new boolean[targetGenerics.length];
                        for (int i = 0; i < targetGenerics.length; i++) {
                            matchers[i] = isAssignableFrom(targetGenerics[i], pretenderGenerics[i]);
                        }
                        int i = 0;
                        boolean areAllMatching = true;
                        while (i < matchers.length) {
                            boolean isMatching = matchers[i++];
                            if (!isMatching) {
                                areAllMatching = false;
                                break;
                            }
                        }
                        return areAllMatching;
                    }
                }
            }
        }
        return false;
    }

    public static List<Field> listFields(Class<?> javaClass, boolean includeSuperClasses) {
        Stream.Builder<Field> fields = Stream.builder();
        reflectFields(javaClass, fields, includeSuperClasses);
        return fields.build().toList();
    }

    public static List<Method> listMethods(Class<?> javaClass, boolean includeSuperClasses) {
        Stream.Builder<Method> methods = Stream.builder();
        reflectMethods(javaClass, methods, includeSuperClasses);
        return methods.build().toList();
    }

    public static List<Method> listSetters(Class<?> javaClass, boolean includeSuperClasses) {
        return listMethods(javaClass, includeSuperClasses)
                .stream()
                .filter(ReflectionUtils::isSetter)
                .toList();
    }

    public static List<Method> listGetters(Class<?> javaClass, boolean includeSuperClasses) {
        return listMethods(javaClass, includeSuperClasses)
                .stream()
                .filter(ReflectionUtils::isGetter)
                .toList();
    }

    public static Optional<Field> findField(Class<?> javaClass, String fieldName, boolean includeSuperClass) {
        return listFields(javaClass, includeSuperClass)
                .stream()
                .filter(field -> fieldName.equals(field.getName()))
                .findFirst();
    }

    public static Optional<Field> findField(Class<?> javaClass,
                                            Type returnedType,
                                            String fieldName,
                                            boolean includeSuperClasses) {
        return listFields(javaClass, includeSuperClasses)
                .stream()
                .filter(field -> fieldName.equals(field.getName()))
                .filter(field -> returnedType.equals(field.getGenericType()))
                .findFirst();
    }

    public static Optional<Method> findGetter(Field field) {
        String getterName = resolveGetterName(field);
        return listMethods(field.getDeclaringClass(), false)
                .stream()
                .filter(method -> isSearchedGetter(method, getterName, field.getGenericType()))
                .findFirst();
    }

    public static Optional<Method> findSetter(Field field) {
        String setterName = resolveSetterName(field);
        return listMethods(field.getDeclaringClass(), false)
                .stream()
                .filter(method -> isSearchedSetter(method, setterName, field.getGenericType()))
                .findFirst();
    }

    public static <T> Optional<Constructor<T>> findNoArgsConstructor(Class<T> javaClass) {
        return Arrays.stream(javaClass.getConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0)
                .map(constructor -> (Constructor<T>) constructor)
                .findFirst();
    }

    public static Collection<Map.Entry<TypeVariable<?>, Type>> mapGenericsBounds(ParameterizedType parameterizedType) {
        Stream.Builder<Map.Entry<TypeVariable<?>, Type>> genericBounds = Stream.builder();
        collectGenericsFromParamType(parameterizedType, genericBounds);
        return genericBounds.build().toList();
    }

    private static void collectGenerics(Type type,
                                        Stream.Builder<Map.Entry<TypeVariable<?>, Type>> genericBounds) {
        if (type instanceof Class<?> javaClass) {
            collectGenericsFromClass(javaClass, genericBounds);
        } else if (type instanceof ParameterizedType parameterizedType) {
            collectGenericsFromParamType(parameterizedType, genericBounds);
        }
    }

    private static void collectGenericsFromClass(Class<?> javaClass,
                                                 Stream.Builder<Map.Entry<TypeVariable<?>, Type>> genericBounds) {
        TypeVariable<? extends Class<?>>[] generics = javaClass.getTypeParameters();
        Arrays.stream(generics).forEach(generic -> Arrays.stream(generic.getBounds())
                .forEach(bound -> genericBounds.add(entry(generic, bound))));
        Type superClass = javaClass.getGenericSuperclass();
        if (superClass != null && !Object.class.equals(superClass)) {
            collectGenerics(superClass, genericBounds);
        }
        Arrays.stream(javaClass.getGenericInterfaces())
                .forEach(interfaceType -> collectGenerics(interfaceType, genericBounds));
    }

    private static void collectGenericsFromParamType(ParameterizedType parameterizedType,
                                                     Stream.Builder<Map.Entry<TypeVariable<?>, Type>> genericBounds) {
        if (parameterizedType.getRawType() instanceof Class<?> javaClass) {
            collectGenericsFromClass(javaClass, genericBounds);
            Type[] actualTypes = parameterizedType.getActualTypeArguments();
            TypeVariable<? extends Class<?>>[] generics = javaClass.getTypeParameters();
            if (actualTypes.length == generics.length) {
                IntStream.range(0, actualTypes.length)
                        .forEach(i -> genericBounds.add(entry(generics[i], actualTypes[i])));
            }
            Type superClass = javaClass.getGenericSuperclass();
            if (superClass != null && !Object.class.equals(superClass)) {
                collectGenerics(superClass, genericBounds);
            }
            Arrays.stream(javaClass.getGenericInterfaces())
                    .forEach(interfaceType -> collectGenerics(interfaceType, genericBounds));
        }
    }

    private static void reflectFields(Class<?> javaClass, Stream.Builder<Field> fields, boolean includeSuperClass) {
        if (javaClass != null) {
            Field[] declaredFields = javaClass.getDeclaredFields();
            IntStream.range(0, declaredFields.length).forEach(i -> fields.add(declaredFields[i]));
            if (includeSuperClass && javaClass.getSuperclass() != null) {
                reflectFields(javaClass.getSuperclass(), fields, includeSuperClass);
            }
        }
    }

    private static void reflectMethods(Class<?> javaClass, Stream.Builder<Method> methods, boolean includeSuperClass) {
        if (javaClass != null) {
            Method[] declaredMethods = javaClass.getDeclaredMethods();
            IntStream.range(0, declaredMethods.length).forEach(i -> methods.add(declaredMethods[i]));
            if (includeSuperClass && javaClass.getSuperclass() != null) {
                reflectMethods(javaClass.getSuperclass(), methods, includeSuperClass);
            }
        }
    }

    private static String resolveGetterName(Field field) {
        Class<?> declaringClass = field.getDeclaringClass();
        Type fieldType = field.getGenericType();
        StringBuilder getterName = new StringBuilder();
        if (!declaringClass.isRecord() && Boolean.TYPE.equals(fieldType)) {
            getterName.append("is");
        } else if (!declaringClass.isRecord()) {
            getterName.append("get");
        }
        if (declaringClass.isRecord()) {
            getterName.append(field.getName());
        } else {
            getterName.append(StringUtils.capitalize(field.getName()));
        }
        return getterName.toString();
    }

    private static String resolveSetterName(Field field) {
        return "set" + StringUtils.capitalize(field.getName());
    }

    private static boolean isSetter(Method method) {
        return isPublicNonStaticMethod(method) &&
                StringUtils.startsWith(method.getName(), "set") &&
                method.getParameterCount() == 1;
    }

    private static boolean isSearchedSetter(Method method, String setterName, Type fieldType) {
        return isPublicNonStaticMethod(method) &&
                StringUtils.equals(method.getName(), setterName) &&
                method.getParameterCount() == 1 &&
                method.getGenericParameterTypes()[0].equals(fieldType);
    }

    private static boolean isGetter(Method method) {
        Type returnedType = method.getGenericReturnType();
        if (Boolean.TYPE.equals(returnedType) && method.getParameterCount() == 0) {
            return isPublicNonStaticMethod(method) && StringUtils.startsWith(method.getName(), "is");
        } else if (method.getParameterCount() == 0) {
            return isPublicNonStaticMethod(method) && StringUtils.startsWith(method.getName(), "get");
        } else {
            return false;
        }
    }

    private static boolean isSearchedGetter(Method method, String getterName, Type fieldType) {
        Type returnedType = method.getGenericReturnType();
        if (returnedType.equals(fieldType) && method.getParameterCount() == 0) {
            return isPublicNonStaticMethod(method) && StringUtils.equals(method.getName(), getterName);
        } else {
            return false;
        }
    }

    private static boolean isPublicNonStaticMethod(Method method) {
        int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers);
    }

}
