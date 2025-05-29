package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.olsonsolution.common.reflection.domain.port.repository.annotion.processor.MessagePrinter;
import com.olsonsolution.common.reflection.domain.port.repository.annotion.processor.TypeElementUtils;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class JpaEntityUtil {

    private final MessagePrinter messagePrinter;

    private final TypeElementUtils typeElementUtils;

    private final Set<Class<? extends Annotation>> mappingAnnotations = Set.of(
            Transient.class, OneToOne.class, OneToMany.class, ManyToOne.class, ManyToMany.class
    );

    Map<String, ColumnElementMetadata> obtainColumnMappings(TypeElement typeElement) {
        Map<String, Column> attributeOverrides = new HashMap<>();
        collectTypeAttributeOverride(typeElement, EMPTY, attributeOverrides);
        Stream.Builder<Map.Entry<String, ColumnElementMetadata>> mappings = Stream.builder();
        collectColumnMappings(typeElement, EMPTY, mappings, attributeOverrides);
        return mappings.build().collect(Collectors.collectingAndThen(
                Collectors.toCollection(LinkedList::new),
                this::toOrderedMap
        ));
    }

    Set<String> obtainEmbeddableIdColumns(TypeElement entityElement) {
        Stream.Builder<VariableElement> mappedFields = Stream.builder();
        collectMappedFields(entityElement, mappedFields);
        return mappedFields.build()
                .filter(entityField -> entityField.getAnnotation(EmbeddedId.class) != null)
                .map(typeElementUtils::getFieldTypeElement)
                .filter(fieldType -> fieldType.getAnnotation(Embeddable.class) != null)
                .findFirst()
                .map(this::obtainColumnMappings)
                .map(Map::keySet)
                .orElseGet(Collections::emptySet);
    }

    String getTableName(Element entityElement) {
        return entityElement.getAnnotation(Table.class) == null ?
                entityElement.getSimpleName().toString() :
                entityElement.getAnnotation(Table.class).name();
    }

    boolean isIdentifier(VariableElement entityFieldElement) {
        return entityFieldElement.getAnnotation(Id.class) != null;
    }

    private void collectColumnMappings(TypeElement typeElement,
                                       String currentPath,
                                       Stream.Builder<Map.Entry<String, ColumnElementMetadata>> mappings,
                                       Map<String, Column> attributeOverrides) {
        Set<VariableElement> typeFields =
                typeElementUtils.getDeclaredVariableElements(typeElement, false);
        for (VariableElement field : typeFields) {
            collectColumnMappings(typeElement, field, currentPath, mappings, attributeOverrides);
        }
        TypeElement mappedSuperClassElement = null;
        TypeMirror superClassMirror = typeElement.getSuperclass();
        if (superClassMirror != null && superClassMirror.getKind() != TypeKind.NONE) {
            mappedSuperClassElement = typeElementUtils.getClassElement(superClassMirror);
        }
        if (mappedSuperClassElement != null && mappedSuperClassElement.getAnnotation(MappedSuperclass.class) != null) {
            collectColumnMappings(mappedSuperClassElement, currentPath, mappings, attributeOverrides);
        }
    }

    private void collectColumnMappings(TypeElement classElement, VariableElement field, String currentPath,
                                       Stream.Builder<Map.Entry<String, ColumnElementMetadata>> mappings,
                                       Map<String, Column> attributeOverrides) {
        try {
            TypeElement fieldType = typeElementUtils.getFieldTypeElement(field);
            String attributePath = field.getSimpleName().toString();
            if (StringUtils.isNotEmpty(currentPath)) {
                attributePath = currentPath + "." + attributePath;
            }
            if (fieldType.getAnnotation(Embeddable.class) != null) {
                collectTypeAttributeOverride(fieldType, EMPTY, attributeOverrides);
                collectColumnMappings(fieldType, attributePath, mappings, attributeOverrides);
            } else if (isFieldColumnMapping(field)) {
                mappings.add(mapToMetadata(field, fieldType, attributePath, attributeOverrides));
            }
        } catch (IllegalArgumentException e) {
            messagePrinter.print(
                    Diagnostic.Kind.WARNING, JpaEntityUtil.class,
                    "Type=%s field=%s can not resolve field type".formatted(classElement, field), e
            );
        }
    }

    private void collectMappedFields(TypeElement typeElement, Stream.Builder<VariableElement> mappedFields) {
        Set<VariableElement> typeFields =
                typeElementUtils.getDeclaredVariableElements(typeElement, false);
        for (VariableElement field : typeFields) {
            if (isFieldColumnMapping(field)) {
                mappedFields.add(field);
            }
        }
        TypeElement mappedSuperClassElement = null;
        TypeMirror superClassMirror = typeElement.getSuperclass();
        if (superClassMirror != null && superClassMirror.getKind() != TypeKind.NONE) {
            mappedSuperClassElement = typeElementUtils.getClassElement(superClassMirror);
        }
        if (mappedSuperClassElement != null && mappedSuperClassElement.getAnnotation(MappedSuperclass.class) != null) {
            collectMappedFields(mappedSuperClassElement, mappedFields);
        }
    }

    private void collectTypeAttributeOverride(TypeElement typeElement, String currentPath,
                                              Map<String, Column> attributeOverrides) {
        collectAttributeOverrides(typeElement, currentPath, attributeOverrides);
        TypeElement mappedSuperClassElement = null;
        TypeMirror superClassMirror = typeElement.getSuperclass();
        if (superClassMirror != null && superClassMirror.getKind() != TypeKind.NONE) {
            mappedSuperClassElement = typeElementUtils.getClassElement(superClassMirror);
        }
        if (mappedSuperClassElement != null && mappedSuperClassElement.getAnnotation(MappedSuperclass.class) != null) {
            collectTypeAttributeOverride(mappedSuperClassElement, currentPath, attributeOverrides);
        }
    }

    private LinkedHashMap<String, ColumnElementMetadata> toOrderedMap(
            List<Map.Entry<String, ColumnElementMetadata>> mappingsList) {
        LinkedHashMap<String, ColumnElementMetadata> mappings = new LinkedHashMap<>(mappingsList.size());
        for (Map.Entry<String, ColumnElementMetadata> mapping : mappingsList) {
            mappings.put(mapping.getKey(), mapping.getValue());
        }
        return mappings;
    }

    private boolean isFieldColumnMapping(VariableElement field) {
        return mappingAnnotations.stream()
                .allMatch(anno -> Objects.isNull(field.getAnnotation(anno)));
    }

    private void collectAttributeOverrides(AnnotatedConstruct annotatedElement,
                                           String currentPath,
                                           Map<String, Column> attributeOverrides) {
        Optional.ofNullable(annotatedElement.getAnnotation(AttributeOverrides.class))
                .stream()
                .flatMap(annotation -> Arrays.stream(annotation.value()))
                .forEach(anno -> appendFromAnnotation(anno, currentPath, attributeOverrides));
        Optional.ofNullable(annotatedElement.getAnnotation(AttributeOverride.class))
                .ifPresent(anno -> appendFromAnnotation(anno, currentPath, attributeOverrides));
    }

    private void appendFromAnnotation(AttributeOverride attributeOverride, String path,
                                      Map<String, Column> attributeOverrides) {
        String attributePath = attributeOverride.name();
        if (StringUtils.isNotEmpty(path)) {
            attributePath = path + "." + attributeOverride.name();
        }
        attributeOverrides.put(attributePath, attributeOverride.column());
        messagePrinter.print(Diagnostic.Kind.NOTE, JpaEntityUtil.class, "Modified attributes override=%s".formatted(attributePath));
    }

    private Map.Entry<String, ColumnElementMetadata> mapToMetadata(VariableElement field, TypeElement fieldType,
                                                                   String currentPath,
                                                                   Map<String, Column> attributeOverrides) {
        Column column;
        if (attributeOverrides.containsKey(currentPath)) {
            column = attributeOverrides.get(currentPath);
        } else {
            column = field.getAnnotation(Column.class);
        }
        String columnName = column == null ? field.getSimpleName().toString() : column.name();
        ColumnElementMetadata columnElementMetadata = new ColumnElementMetadata(field, fieldType, column);
        return new DefaultMapEntry<>(columnName, columnElementMetadata);
    }

}
