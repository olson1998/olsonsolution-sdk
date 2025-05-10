package com.olsonsolution.common.spring.application.annotation.processor.migration;

import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

import static javax.lang.model.element.ElementKind.CLASS;

@RequiredArgsConstructor
class TableMetadataUtil {

    private final ProcessingEnvironment processingEnv;

    String resolveTableName(Element element) {
        return element.getAnnotation(Table.class) == null ?
                element.getSimpleName().toString() :
                element.getAnnotation(Table.class).name();
    }

    String resolveColumnName(VariableElement variableElement) {
        return variableElement.getAnnotation(Column.class) == null ?
                variableElement.getSimpleName().toString() :
                variableElement.getAnnotation(Column.class).name();
    }

    String resolveType(VariableElement variableElement, Column column) {
        if (column != null && !column.columnDefinition().isEmpty()) {
            return column.columnDefinition();
        }
        return assumeType(variableElement, column);
    }

    String assumeType(VariableElement variableElement, Column column) {
        TypeMirror variableTypeMirror = variableElement.asType();
        if (variableTypeMirror.getKind().isPrimitive()) {
            return assumePrimitiveType(variableTypeMirror);
        } else if (isAssignableFieldType(variableElement, Integer.class)) {
            return "INT";
        } else if (isAssignableFieldType(variableElement, Long.class) ||
                isAssignableFieldType(variableElement, BigInteger.class)) {
            return "BIGINT";
        } else if (isAssignableFieldType(variableElement, Short.class)) {
            return "SMALLINT";
        } else if (isAssignableFieldType(variableElement, Double.class) ||
                isAssignableFieldType(variableElement, BigDecimal.class)) {
            return "double";
        } else if (isAssignableFieldType(variableElement, Float.class)) {
            return "float";
        } else if (isAssignableFieldType(variableElement, Boolean.class)) {
            return "boolean";
        } else if (isAssignableFieldType(variableElement, Character.class)) {
            return "varchar(1)";
        } else if (isAssignableFieldType(variableElement, String.class) && column != null) {
            return "varchar(" + column.length() + ")";
        } else if (isAssignableFieldType(variableElement, String.class)) {
            return "varchar(255)";
        } else if (isAssignableFieldType(variableElement, UUID.class)) {
            return "varchar(36)";
        } else {
            return "";
        }
    }

    String assumePrimitiveType(TypeMirror typeMirror) {
        switch (typeMirror.getKind()) {
            case INT -> {
                return "INT";
            }
            case LONG -> {
                return "BIGINT";
            }
            case SHORT -> {
                return "SMALLINT";
            }
            case DOUBLE -> {
                return "double";
            }
            case FLOAT -> {
                return "float";
            }
            case BOOLEAN -> {
                return "boolean";
            }
            case CHAR -> {
                return "varchar(1)";
            }
        }
        return "";
    }

    boolean isJpaEntity(Element element) {
        return element.getAnnotation(Entity.class) != null && element.getKind() == CLASS;
    }

    boolean isEmbeddable(VariableElement variableElement) {
        if (processingEnv.getTypeUtils().asElement(variableElement.asType()) instanceof TypeElement fieldTypeElement) {
            return fieldTypeElement.getAnnotation(Embeddable.class) != null;
        }
        return false;
    }

    boolean isIdentifier(VariableElement variableElement) {
        return variableElement.getAnnotation(Id.class) != null;
    }

    boolean isEmbeddableIdentifier(VariableElement variableElement) {
        return variableElement.getAnnotation(EmbeddedId.class) != null;
    }

    private boolean isAssignableFieldType(VariableElement element, Class<?> javaClass) {
        Types typeUtils = processingEnv.getTypeUtils();
        Elements elementUtils = processingEnv.getElementUtils();
        TypeMirror typeMirror = getDeclaredType(javaClass, typeUtils, elementUtils);
        return typeUtils.isAssignable(element.asType(), typeMirror);
    }

    private TypeMirror getDeclaredType(Class<?> javaClass, Types typeUtils, Elements elementUtils) {
        TypeElement typeElement = elementUtils.getTypeElement(javaClass.getCanonicalName());
        return typeUtils.getDeclaredType(typeElement);
    }

}
