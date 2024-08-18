package com.olsonsolution.common.reflection.domain.service;

import com.olsonsolution.common.reflection.domain.repository.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Map.entry;

public class ReflectionService implements ReflectionUtils {

    public static final ReflectionUtils SINGLETON = new ReflectionService();

    @Override
    public boolean isAssignableFrom(Type targetType, Type pretenderType) {
        if (targetType instanceof Class<?> targetClass && pretenderType instanceof Class<?> pretenderClass) {
            return pretenderClass.isAssignableFrom(targetClass);
        } else if (targetType instanceof ParameterizedType targetParamType &&
                pretenderType instanceof Class<?> pretenderClass) {
            if(targetParamType.getRawType() instanceof Class<?> targetRawClass) {
                return pretenderClass.isAssignableFrom(targetRawClass);
            }
        } else if (targetType instanceof ParameterizedType targetParamType &&
                pretenderType instanceof ParameterizedType pretenderParamType) {
           if(targetParamType.getRawType() instanceof Class<?> targetRawClass &&
            pretenderParamType.getRawType() instanceof Class<?> pretenderRawClass) {
                if(pretenderRawClass.isAssignableFrom(targetRawClass)) {
                    Type[] targetGenerics = targetParamType.getActualTypeArguments();
                    Type[] pretenderGenerics = pretenderParamType.getActualTypeArguments();
                    if(targetGenerics.length == pretenderGenerics.length) {
                        boolean[] matchers = new boolean[targetGenerics.length];
                        for(int i = 0; i < targetGenerics.length; i++) {
                            matchers[i] = isAssignableFrom(targetGenerics[i], pretenderGenerics[i]);
                        }
                        int i = 0;
                        boolean areAllMatching = true;
                        while (i < matchers.length) {
                            boolean isMatching = matchers[i++];
                            if(!isMatching) {
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

    @Override
    public List<Field> listFields(Class<?> javaClass, boolean includeSuperClasses) {
        List<Field> fields = new ArrayList<>();
        reflectFields(javaClass, fields, includeSuperClasses);
        return fields;
    }

    @Override
    public List<Method> listMethods(Class<?> javaClass, boolean includeSuperClasses) {
        List<Method> methods = new ArrayList<>();
        reflectMethods(javaClass, methods, includeSuperClasses);
        return methods;
    }

    @Override
    public Optional<Field> findField(Class<?> javaClass, String fieldName, boolean includeSuperClass) {
        return listFields(javaClass, includeSuperClass)
                .stream()
                .filter(field -> fieldName.equals(field.getName()))
                .findFirst();
    }

    @Override
    public Optional<Field> findField(Class<?> javaClass, Type returnedType, String fieldName, boolean includeSuperClasses) {
        return listFields(javaClass, includeSuperClasses)
                .stream()
                .filter(field -> fieldName.equals(field.getName()))
                .filter(field -> returnedType.equals(field.getGenericType()))
                .findFirst();
    }

    @Override
    public Optional<Method> findGetter(Field field) {
        String getterName = resolveGetterName(field);
        return listMethods(field.getDeclaringClass(), false)
                .stream()
                .filter(method -> getterName.equals(method.getName())&&
                        field.getGenericType().equals(method.getGenericReturnType()))
                .findFirst();
    }

    @Override
    public Optional<Method> findSetter(Field field) {
        String setterName = resolveSetterName(field);
        return listMethods(field.getDeclaringClass(), false)
                .stream()
                .filter(method -> setterName.equals(method.getName()) &&
                        Void.TYPE.equals(method.getGenericReturnType()) &&
                        method.getParameterCount() == 1 &&
                        field.getGenericType().equals(method.getParameters()[0].getParameterizedType()))
                .findFirst();
    }

    @Override
    public <T> Optional<Constructor<T>> findNoArgsConstructor(Class<T> javaClass) {
        return Arrays.stream(javaClass.getConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0)
                .map(constructor -> (Constructor<T>) constructor)
                .findFirst();
    }

    @Override
    public Map<TypeVariable<?>, Type> mapGenericsBounds(ParameterizedType parameterizedType) {
        Stream.Builder<Map.Entry<TypeVariable<?>, Type>> genericBounds = Stream.builder();
        if(parameterizedType.getRawType() instanceof Class<?> rawClass) {
            Type[] actualTypes = parameterizedType.getActualTypeArguments();
            TypeVariable<? extends Class<?>>[] generics = rawClass.getTypeParameters();
            for(int i = 0; i < generics.length; i++) {
                genericBounds.add(entry(generics[i], actualTypes[i]));
            }
        }
        return genericBounds.build()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void reflectFields(Class<?> javaClass, List<Field> classFields, boolean includeSuperClass) {
        classFields.addAll(Arrays.asList(javaClass.getDeclaredFields()));
        if (includeSuperClass && classFields.stream().noneMatch(field -> field.getDeclaringClass().equals(javaClass.getSuperclass()))) {
            reflectFields(javaClass.getSuperclass(), classFields, includeSuperClass);
        }
    }

    private void reflectMethods(Class<?> javaClass, List<Method> methods, boolean includeSuperClass) {
        methods.addAll(Arrays.asList(javaClass.getDeclaredMethods()));
        if (includeSuperClass &&
                methods.stream().noneMatch(m -> m.getDeclaringClass().equals(javaClass.getSuperclass()))) {
            reflectMethods(javaClass.getSuperclass(), methods, includeSuperClass);
        }
    }

    private String resolveGetterName(Field field) {
        Class<?> declaringClass = field.getDeclaringClass();
        Type fieldType = field.getGenericType();
        StringBuilder getterName = new StringBuilder();
        if(!declaringClass.isRecord() && Boolean.TYPE.equals(fieldType)) {
            getterName.append("is");
        } else if (!declaringClass.isRecord()) {
            getterName.append("get");
        }
        if(declaringClass.isRecord()) {
            getterName.append(field.getName());
        } else {
            getterName.append(StringUtils.capitalize(field.getName()));
        }
        return getterName.toString();
    }

    private String resolveSetterName(Field field) {
        return "set" + StringUtils.capitalize(field.getName());
    }

}
