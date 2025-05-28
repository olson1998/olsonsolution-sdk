package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.olsonsolution.common.reflection.domain.port.repository.annotion.processor.MessagePrinter;
import com.olsonsolution.common.reflection.domain.port.repository.annotion.processor.TypeElementUtils;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class JpaEntityUtil {

    private final MessagePrinter messagePrinter;

    private final TypeElementUtils typeElementUtils;

    Map<String, VariableElement> obtainColumnMappings(TypeElement typeElement) {
        Stream.Builder<Map.Entry<String, VariableElement>> mappings = Stream.builder();
        collectColumnMappings(typeElement, mappings);
        return mappings.build().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    String getTableName(Element entityElement) {
        return entityElement.getAnnotation(Table.class) == null ?
                entityElement.getSimpleName().toString() :
                entityElement.getAnnotation(Table.class).name();
    }

    String getColumnName(VariableElement entityFieldElement) {
        return entityFieldElement.getAnnotation(Column.class) == null ?
                entityFieldElement.getSimpleName().toString() :
                entityFieldElement.getAnnotation(Column.class).name();
    }

    boolean isIdentifier(VariableElement entityFieldElement) {
        return entityFieldElement.getAnnotation(Id.class) != null;
    }

    boolean isEmbeddableIdentifier(VariableElement entityFieldElement) {
        return entityFieldElement.getAnnotation(EmbeddedId.class) != null;
    }

    private void collectColumnMappings(TypeElement typeElement,
                                       Stream.Builder<Map.Entry<String, VariableElement>> mappings) {
        Set<VariableElement> typeFields = typeElementUtils.getDeclaredVariableElements(typeElement, false);
        for (VariableElement field : typeFields) {
            collectFieldMappings(typeElement, field, mappings);
        }
        TypeElement mappedSuperClassElement = null;
        TypeMirror superClassMirror = typeElement.getSuperclass();
        if (superClassMirror != null && superClassMirror.getKind() != TypeKind.NONE) {
            mappedSuperClassElement = typeElementUtils.getClassElement(superClassMirror);
        }
        if (mappedSuperClassElement != null && mappedSuperClassElement.getAnnotation(MappedSuperclass.class) != null) {
            collectColumnMappings(mappedSuperClassElement, mappings);
        }
    }

    private void collectFieldMappings(TypeElement classElement, VariableElement field,
                                      Stream.Builder<Map.Entry<String, VariableElement>> mappings) {
        try {
            TypeElement fieldType = typeElementUtils.getFieldTypeElement(field);
            if (fieldType.getAnnotation(Embeddable.class) != null) {
                collectColumnMappings(fieldType, mappings);
            } else {
                String columnName = getColumnName(field);
                mappings.add(new DefaultMapEntry<>(columnName, field));
            }
        } catch (IllegalArgumentException e) {
            messagePrinter.print(
                    Diagnostic.Kind.WARNING, JpaEntityUtil.class,
                    "Type=%s field=%s can not resolve field type".formatted(classElement, field), e
            );
        }
    }

}
