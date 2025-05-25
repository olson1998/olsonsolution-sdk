package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.olsonsolution.common.spring.application.annotation.migration.ChangeSet;
import com.olsonsolution.common.spring.application.annotation.migration.ColumnChange;
import com.olsonsolution.common.spring.application.annotation.migration.ColumnChanges;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.*;
import java.util.stream.Stream;

class JpaSpecAnnotationUtils {

    List<ColumnChange> listColumnChanges(TypeElement entityElement,
                                         Set<VariableElement> entityFields) {
        ChangeSet changeSet = entityElement.getAnnotation(ChangeSet.class);
        if (changeSet == null) {
            return Collections.emptyList();
        }
        Stream<ColumnChange> typeAnnotations = streamTypeAnnotation(entityElement);
        Stream<ColumnChange> fieldsAnnotations = streamFieldsAnnotations(entityFields);
        return Stream.concat(typeAnnotations, fieldsAnnotations)
                .toList();
    }

    List<ColumnChange> listColumnChangesForField(TypeElement entityElement,
                                                 VariableElement entityField,
                                                 String column) {
        ChangeSet changeSet = entityElement.getAnnotation(ChangeSet.class);
        if (changeSet == null) {
            return Collections.emptyList();
        }
        Stream<ColumnChange> typeAnnotations = streamTypeAnnotation(entityElement)
                .filter(columnChange -> StringUtils.equals(columnChange.column(), column));
        Stream<ColumnChange> fieldAnnotations = streamFieldAnnotations(entityField);
        return Stream.concat(typeAnnotations, fieldAnnotations)
                .toList();
    }

    String generateId(ChangeSet changeSet, String version, String tableName) {
        String id = changeSet.id();
        id = id.replace("{version}", version);
        id = id.replace("{table}", tableName);
        return id;
    }

    String getParameter(ColumnChange columnChange, String parameterName) {
        return Arrays.stream(columnChange.parameters())
                .filter(parameter -> StringUtils.equals(parameter.name(), parameterName))
                .findFirst()
                .map(ColumnChange.Parameter::value)
                .orElseThrow();
    }

    private Stream<ColumnChange> streamTypeAnnotation(TypeElement typeElement) {
        return Optional.ofNullable(typeElement.getAnnotation(ColumnChanges.class))
                .stream()
                .flatMap(this::parseAnnotation);
    }

    private Stream<ColumnChange> streamFieldsAnnotations(Set<VariableElement> entityFields) {
        return entityFields.stream().flatMap(this::streamFieldAnnotations);
    }

    private Stream<ColumnChange> streamFieldAnnotations(VariableElement entityField) {
        return Optional.ofNullable(entityField.getAnnotation(ColumnChanges.class))
                .stream()
                .flatMap(this::parseAnnotation);
    }

    private Stream<ColumnChange> parseAnnotation(ColumnChanges columnChanges) {
        Stream<ColumnChange> atBeginning = Arrays.stream(columnChanges.atBeginning());
        Stream<ColumnChange> atEnd = Arrays.stream(columnChanges.atEnd());
        return Stream.concat(atBeginning, atEnd);
    }

}
