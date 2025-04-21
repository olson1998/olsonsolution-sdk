package com.olsonsolution.common.spring.application.datasource.migration.annotation.processor;

import com.google.auto.service.AutoService;
import com.olsonsolution.common.spring.application.datasource.migration.annotation.ChangeSet;
import com.olsonsolution.common.spring.application.datasource.migration.annotation.ColumnChange;
import com.olsonsolution.common.spring.application.datasource.migration.annotation.ColumnChanges;
import com.olsonsolution.common.spring.application.datasource.migration.annotation.Operation;
import jakarta.persistence.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        changeSetEntities.forEach((typeElement, changeSet) -> processChangeSet(
                typeElement,
                changeSet,
                roundEnv
        ));
        return true;
    }

    private void processChangeSet(TypeElement typeElement, ChangeSet changeSet, RoundEnvironment roundEnv) {
        String changeLogPath = changeSet.path();

    }

    private Map<String, List<ChangeSetOperation>> collectColumnsChanges(TypeElement typeElement) {
        Map<String, List<ChangeSetOperation>> changeSetAtBeginningOperations = new HashMap<>();
        Map<String, List<ChangeSetOperation>> changeSetColumnOperations = new HashMap<>();
        Map<String, List<ChangeSetOperation>> changeSetAtEndOperations = new HashMap<>();
        Map<String, List<ChangeSetOperation>> changeSetOperations = new LinkedHashMap<>();
        Set<VariableElement> fields = ElementFilter.fieldsIn(Collections.singleton(typeElement));
        String tableName = resolveTableName(typeElement);
        fields.forEach(fieldElement -> collectColumnsChanges(
                fieldElement,
                tableName,
                changeSetAtBeginningOperations,
                changeSetColumnOperations,
                changeSetAtEndOperations
        ));
        List<String> changeSetVersion = resolveVersions(
                changeSetAtBeginningOperations,
                changeSetColumnOperations,
                changeSetAtEndOperations
        );
        changeSetVersion.forEach(version -> collectOperations(
                version,
                changeSetAtBeginningOperations,
                changeSetColumnOperations,
                changeSetAtEndOperations,
                changeSetOperations
        ));
        return changeSetOperations;
    }

    private void collectOperations(String version,
                                   Map<String, List<ChangeSetOperation>> changeSetAtBeginningOperations,
                                   Map<String, List<ChangeSetOperation>> changeSetColumnOperations,
                                   Map<String, List<ChangeSetOperation>> changeSetAtEndOperations,
                                   Map<String, List<ChangeSetOperation>> changeSetOperations) {
        List<ChangeSetOperation> operations =
                changeSetOperations.computeIfAbsent(version, k -> new LinkedList<>());
        Optional.ofNullable(changeSetAtBeginningOperations.get(version))
                .ifPresent(operations::addAll);
        Optional.ofNullable(changeSetColumnOperations.get(version))
                .ifPresent(operations::addAll);
        Optional.ofNullable(changeSetAtEndOperations.get(version))
                .ifPresent(operations::addAll);
    }

    private void collectColumnsChanges(VariableElement fieldElement,
                                       String tableName,
                                       Map<String, List<ChangeSetOperation>> changeSetAtBeginningOperations,
                                       Map<String, List<ChangeSetOperation>> changeSetOperations,
                                       Map<String, List<ChangeSetOperation>> changeSetAtEndOperations) {
        if (isEmbeddable(fieldElement)) {

        } else {
            collectColumnOperations(
                    fieldElement,
                    tableName,
                    changeSetAtBeginningOperations,
                    changeSetOperations,
                    changeSetAtEndOperations
            );
        }
    }

    private Map<String, List<ChangeSetOperation>> collectColumnOperations(
            VariableElement fieldElement,
            String tableName,
            Map<String, List<ChangeSetOperation>> changeSetAtBeginningOperations,
            Map<String, List<ChangeSetOperation>> changeSetOperations,
            Map<String, List<ChangeSetOperation>> changeSetAtEndOperations) {
        String columnName = resolveColumnName(fieldElement);
        collectColumnOperations(
                fieldElement,
                tableName,
                columnName,
                changeSetAtBeginningOperations,
                changeSetOperations,
                changeSetAtEndOperations
        );
        return changeSetOperations;
    }

    private void collectColumnOperations(VariableElement fieldElement,
                                         String tableName,
                                         String columnName,
                                         Map<String, List<ChangeSetOperation>> changeSetAtBeginningOperations,
                                         Map<String, List<ChangeSetOperation>> changeSetOperations,
                                         Map<String, List<ChangeSetOperation>> changeSetAtEndOperations) {
        collectAddColumnOperations(
                fieldElement,
                tableName,
                columnName,
                fieldElement.getAnnotation(Column.class),
                fieldElement.getAnnotation(ColumnChange.class),
                changeSetOperations
        );
        Optional.ofNullable(fieldElement.getAnnotation(ColumnChanges.class))
                .ifPresent(changes -> collectColumnChangesOperations(
                        changes,
                        tableName,
                        columnName,
                        changeSetAtBeginningOperations,
                        changeSetAtEndOperations
                ));
    }

    private void collectAddColumnOperations(VariableElement fieldElement,
                                            String tableName,
                                            String columnName,
                                            Column column,
                                            ColumnChange columnChange,
                                            Map<String, List<ChangeSetOperation>> changeSetOperations) {
        String firstVersion = columnChange == null ? DEFAULT_VERSION : columnChange.version();
        List<ChangeSetOperation> operations =
                changeSetOperations.computeIfAbsent(firstVersion, k -> new LinkedList<>());
        AddColumnOp.AddColumnOpBuilder addColumnOp = AddColumnOp.builder()
                .column(columnName);
        if (isIdentifier(fieldElement)) {
            addColumnOp.constraint(ConstraintMetadata.builder()
                    .name("ph_" + tableName)
                    .type(PRIMARY_KEY)
                    .build());
        }
        if (column != null) {
            if (column.unique()) {
                addColumnOp.constraint(ConstraintMetadata.builder()
                        .name("unique_" + tableName + "_" + columnName)
                        .type(UNIQUE)
                        .build());
            }
            if (column.nullable()) {
                addColumnOp.constraint(ConstraintMetadata.builder()
                        .name("nonnull_" + tableName + '_' + columnName)
                        .type(NULLABLE_FALSE)
                        .build());
            }
        }
        operations.add(addColumnOp.build());
    }

    private void collectColumnChangesOperations(ColumnChanges columnChanges,
                                                String tableName,
                                                String columnName,
                                                Map<String, List<ChangeSetOperation>> changeSetAtBeginningOperations,
                                                Map<String, List<ChangeSetOperation>> changeSetAtEndOperations) {
        Arrays.stream(columnChanges.atBeginning()).forEach(columnChange -> collectAtBeginningOperations(
                columnChange,
                tableName,
                columnName,
                changeSetAtBeginningOperations
        ));
        Arrays.stream(columnChanges.atEnd()).forEach(columnChange -> collectAtEndOperations(
                columnChange,
                tableName,
                columnName,
                changeSetAtEndOperations
        ));
    }

    private void collectAtBeginningOperations(ColumnChange columnChange,
                                              String tableName,
                                              String columnName,
                                              Map<String, List<ChangeSetOperation>> changeSetAtBeginningOperations) {
        List<ChangeSetOperation> atBeginningOperations =
                changeSetAtBeginningOperations.computeIfAbsent(columnChange.version(), k -> new LinkedList<>());
        atBeginningOperations.addAll(buildOperations(columnChange.operation(), tableName, columnName, columnChange));
    }

    private void collectAtEndOperations(ColumnChange columnChange,
                                        String tableName,
                                        String columnName,
                                        Map<String, List<ChangeSetOperation>> changeSetAtEndOperations) {
        List<ChangeSetOperation> atEndOperations =
                changeSetAtEndOperations.computeIfAbsent(columnChange.version(), k -> new LinkedList<>());
        atEndOperations.addAll(buildOperations(columnChange.operation(), tableName, columnName, columnChange));
    }

    private List<ChangeSetOperation> buildOperations(Operation operation,
                                                     String tableName,
                                                     String columnName,
                                                     ColumnChange columnChange) {
        if (operation == Operation.REMOVE) {
            return Collections.emptyList();
        } else if (operation == Operation.DEFAULT_VALUE_CHANGE) {
            List<ChangeSetOperation> operations = new LinkedList<>();
            operations.add(new DropDefaultValueOp(tableName, columnName));
            operations.add(new AddDefaultValueOp(tableName, columnName, columnChange.parameters()));
            return operations;
        } else if (operation == Operation.NULLABILITY_CHANGE) {
            return Collections.singletonList(new DropNotNullConstraintOp(tableName, columnName));
        } else if (operation == Operation.TYPE_CHANGE) {
            return Collections.singletonList(new ModifyDataTypeOp(tableName, columnName, columnChange.parameters()));
        } else {
            return Collections.emptyList();
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

    private List<String> resolveVersions(Map<String, List<ChangeSetOperation>> changeSetAtBeginningOperations,
                                         Map<String, List<ChangeSetOperation>> changeSetOperations,
                                         Map<String, List<ChangeSetOperation>> changeSetAtEndOperations) {
        Stream.Builder<String> versions = Stream.builder();
        changeSetAtBeginningOperations.keySet().forEach(versions::add);
        changeSetOperations.keySet().forEach(versions::add);
        changeSetAtEndOperations.keySet().forEach(versions::add);
        return versions.build()
                .distinct()
                .sorted(Comparator.comparing(s -> s))
                .collect(Collectors.toCollection(LinkedList::new));
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
