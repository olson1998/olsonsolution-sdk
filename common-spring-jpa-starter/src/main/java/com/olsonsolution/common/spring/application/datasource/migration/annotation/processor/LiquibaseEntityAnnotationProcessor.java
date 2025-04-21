package com.olsonsolution.common.spring.application.datasource.migration.annotation.processor;

import com.google.auto.service.AutoService;
import com.olsonsolution.common.spring.application.datasource.migration.annotation.ChangeSet;
import com.olsonsolution.common.spring.application.datasource.migration.annotation.ColumnChange;
import jakarta.persistence.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import java.util.*;
import java.util.stream.Collectors;

import static com.olsonsolution.common.spring.application.datasource.migration.annotation.processor.ConstraintMetadata.Type.*;
import static java.util.Map.entry;
import static javax.lang.model.element.ElementKind.CLASS;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({
        "com.olsonsolution.common.spring.application.datasource.migration.annotation.ChangeSet"
})
public class LiquibaseEntityAnnotationProcessor extends AbstractProcessor {

    private static final String DEFAULT_VERSION = "1.0";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<TypeElement, ChangeSet> changeSetEntities = getJpaEntities(roundEnv);
        return true;
    }

    private void processChangeSetEntity(TypeElement typeElement, ChangeSet changeSet, RoundEnvironment roundEnv) {
        String changeLogPath = changeSet.path();

    }

    private void collectColumnsChanges(TypeElement typeElement) {
        Set<VariableElement> fields = ElementFilter.fieldsIn(Collections.singleton(typeElement));
        String tableName = resolveTableName(typeElement);
    }

    private void collectColumnsChanges(TypeElement typeElement,
                                       VariableElement fieldElement,
                                       String tableName) {
        if (isEmbeddable(fieldElement)) {

        } else {

        }
    }

    private void collectAddColumn(TypeElement typeElement,
                                   VariableElement fieldElement,
                                   String tableName) {
        Column columnMetadata = fieldElement.getAnnotation(Column.class);
        String columnName = resolveColumnName(fieldElement);
        String changeSetVersion = resolveChangeSetVersion(fieldElement);
        if (isIdentifier(fieldElement)) {

        }
        if (columnMetadata != null) {
            if (columnMetadata.unique()) {

            }
        }
    }

    private Map<TypeElement, ChangeSet> getJpaEntities(RoundEnvironment roundEnv) {
        return roundEnv.getElementsAnnotatedWith(ChangeSet.class)
                .stream()
                .filter(this::isJpaEntity)
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .map(element -> entry(element, element.getAnnotation(ChangeSet.class)))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private String resolveChangeSetVersion(VariableElement fieldElement) {
        return Optional.ofNullable(fieldElement.getAnnotation(ColumnChange.class))
                .map(ColumnChange::version)
                .orElse(DEFAULT_VERSION);
    }

    private String resolveTableName(Element element) {
        return element.getAnnotation(Table.class) == null ?
                element.getSimpleName().toString() :
                element.getAnnotation(Table.class).name();
    }

    private String resolveColumnName(VariableElement variableElement) {
        return variableElement.getAnnotation(Column.class) == null ?
                variableElement.getSimpleName().toString() :
                variableElement.getAnnotation(Column.class).name();
    }

    private boolean isJpaEntity(Element element) {
        return element.getAnnotation(Entity.class) != null && element.getKind() == CLASS;
    }

    private boolean isEmbeddable(VariableElement variableElement) {
        return variableElement.getAnnotation(Embeddable.class) != null;
    }

    private boolean isIdentifier(VariableElement variableElement) {
        return variableElement.getAnnotation(Id.class) != null;
    }

    private boolean isEmbeddableIdentifier(VariableElement variableElement) {
        return variableElement.getAnnotation(EmbeddedId.class) != null;
    }

}
