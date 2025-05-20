package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.olsonsolution.common.spring.application.annotation.migration.*;
import jakarta.persistence.Column;
import jakarta.persistence.SequenceGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.lang.model.element.ElementKind.FIELD;

@RequiredArgsConstructor
class JpaSpecProcedureFactory {

    private final ProcessingEnvironment processingEnv;

    private final TableMetadataUtil tableMetadataUtil;

    JpaSpecExecPlan fabricate(List<JpaSpecMetadata> jpaSpecsMetadata) {
        List<JpaSpecProcedure> procedures = jpaSpecsMetadata.stream()
                .map(jpaSpecMetadata -> createProcedure(jpaSpecMetadata, jpaSpecsMetadata))
                .toList();
        LinkedHashSet<String> jpaSpecNames = procedures.stream()
                .map(JpaSpecProcedure::metadata)
                .map(JpaSpecMetadata::jpaSpec)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        ChangeLogOrder order = new ChangeLogOrder(procedures);
        LinkedHashSet<String> changeLogOrder = procedures.stream()
                .flatMap(procedure -> procedure.changeSets()
                        .stream()
                        .map(changeSetOp -> new DefaultMapEntry<>(procedure, changeSetOp)))
                .sorted(Comparator.comparing(
                        changeSetOp -> changeSetOp.getValue().id(),
                        order
                )).map(changeSetOp ->
                        changeSetOp.getKey().metadata().jpaSpec() + '/' + changeSetOp.getValue().id() + ".xml"
                ).collect(Collectors.toCollection(LinkedHashSet::new));
        return new JpaSpecExecPlan(procedures, jpaSpecNames, changeLogOrder);
    }

    private JpaSpecProcedure createProcedure(JpaSpecMetadata jpaSpecMetadata,
                                             List<JpaSpecMetadata> jpaSpecsMetadata) {
        Stream.Builder<ChangeSetOp> changeSets = Stream.builder();
        jpaSpecMetadata.entitiesConfig().forEach(entityConfig -> collectChangeSetOps(
                entityConfig,
                jpaSpecMetadata, jpaSpecsMetadata, changeSets
        ));
        return changeSets.build()
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        changeSetOps -> JpaSpecProcedure.builder()
                                .metadata(jpaSpecMetadata)
                                .changeSets(changeSetOps)
                                .build()
                ));
    }

    private void collectChangeSetOps(EntityConfig entityConfig,
                                     JpaSpecMetadata jpaSpecMetadata,
                                     List<JpaSpecMetadata> jpaSpecsMetadata,
                                     Stream.Builder<ChangeSetOp> changeSets) {
        ChangeSet changeSet = entityConfig.entity().getAnnotation(ChangeSet.class);
        collectChangeSetOps(entityConfig, jpaSpecMetadata, jpaSpecsMetadata, changeSet, changeSets);
    }

    private void collectChangeSetOps(EntityConfig entityConfig,
                                     JpaSpecMetadata jpaSpecMetadata,
                                     List<JpaSpecMetadata> jpaSpecsMetadata,
                                     ChangeSet changeSet,
                                     Stream.Builder<ChangeSetOp> changeSets) {
        Map<String, List<ChangeOp>> changeSetAtBeginningOperations = new HashMap<>();
        Map<String, List<ChangeOp>> changeSetCreateTableOperations = new HashMap<>();
        Map<String, List<ChangeOp>> changeSetAtEndOperations = new HashMap<>();
        Map<String, LinkedList<ChangeOp>> changeSetOperations = new LinkedHashMap<>();
        Set<VariableElement> fields = getDeclaredFields(entityConfig.entity());
        collectColumnsChanges(
                fields,
                entityConfig,
                jpaSpecMetadata.jpaSpec(),
                changeSet,
                changeSetAtBeginningOperations,
                changeSetCreateTableOperations,
                changeSetAtEndOperations
        );
        List<String> changeSetVersion = resolveVersions(
                changeSetAtBeginningOperations,
                changeSetCreateTableOperations,
                changeSetAtEndOperations
        );
        changeSetVersion.forEach(version -> collectOperations(
                version,
                changeSetAtBeginningOperations,
                changeSetCreateTableOperations,
                changeSetAtEndOperations,
                changeSetOperations
        ));
        changeSetOperations.forEach((version, ops) -> collectChangeSetOps(
                version,
                ops,
                jpaSpecMetadata,
                jpaSpecsMetadata,
                changeSet,
                entityConfig,
                fields,
                changeSets
        ));
    }

    private void collectChangeSetOps(String version,
                                     List<ChangeOp> operations,
                                     JpaSpecMetadata jpaSpecMetadata,
                                     List<JpaSpecMetadata> jpaSpecsMetadata,
                                     ChangeSet changeSet,
                                     EntityConfig entityConfig,
                                     Set<VariableElement> fields,
                                     Stream.Builder<ChangeSetOp> changeSets) {
        Map<String, Set<String>> dependsOn = collectDependsOn(changeSet, fields, jpaSpecMetadata, jpaSpecsMetadata);
        ChangeSetOp changeSetOp = ChangeSetOp.builder()
                .table(entityConfig.table())
                .version(version)
                .id(generateId(changeSet, version, entityConfig.table()))
                .operations(operations)
                .dependsOn(dependsOn)
                .build();
        changeSets.add(changeSetOp);
    }

    private void collectOperations(String version,
                                   Map<String, List<ChangeOp>> changeSetAtBeginningOperations,
                                   Map<String, List<ChangeOp>> changeSetColumnOperations,
                                   Map<String, List<ChangeOp>> changeSetAtEndOperations,
                                   Map<String, LinkedList<ChangeOp>> changeSetOperations) {
        List<ChangeOp> operations =
                changeSetOperations.computeIfAbsent(version, k -> new LinkedList<>());
        Optional.ofNullable(changeSetAtBeginningOperations.get(version))
                .ifPresent(operations::addAll);
        Optional.ofNullable(changeSetColumnOperations.get(version))
                .ifPresent(operations::addAll);
        Optional.ofNullable(changeSetAtEndOperations.get(version))
                .ifPresent(operations::addAll);
    }

    private Map<String, Set<String>> collectDependsOn(ChangeSet changeSet,
                                                      Set<VariableElement> fields,
                                                      JpaSpecMetadata jpaSpecMetadata,
                                                      List<JpaSpecMetadata> jpaSpecsMetadata) {
        Map<String, Set<String>> dependencies = new HashMap<>();
        collectForeignKeyDependencies(fields, jpaSpecMetadata, jpaSpecsMetadata, dependencies);
        collectChangeSetDependencies(changeSet, jpaSpecMetadata, jpaSpecsMetadata, dependencies);
        return dependencies;
    }

    private void collectForeignKeyDependencies(Set<VariableElement> fields,
                                               JpaSpecMetadata jpaSpecMetadata,
                                               List<JpaSpecMetadata> jpaSpecsMetadata,
                                               Map<String, Set<String>> dependencies) {
        fields.stream()
                .filter(fieldElement -> fieldElement.getAnnotation(ForeignKey.class) != null)
                .map(fieldElement -> fieldElement.getAnnotation(ForeignKey.class))
                .map(foreignKey -> mapToJpaSpecDependency(foreignKey, jpaSpecMetadata, jpaSpecsMetadata))
                .forEach(jpaSpecChangeLog -> dependencies.computeIfAbsent(
                        jpaSpecChangeLog.getKey(),
                        s -> new HashSet<>()
                ).add(jpaSpecChangeLog.getValue()));
    }

    private void collectChangeSetDependencies(ChangeSet changeSet,
                                              JpaSpecMetadata jpaSpecMetadata,
                                              List<JpaSpecMetadata> jpaSpecsMetadata,
                                              Map<String, Set<String>> dependencies) {
        Arrays.stream(changeSet.dependsOn())
                .map(dependsOn -> mapToJpaSpecDependency(dependsOn, jpaSpecMetadata, jpaSpecsMetadata))
                .forEach(jpaSpecChangeLog -> dependencies.computeIfAbsent(
                        jpaSpecChangeLog.getKey(),
                        s -> new HashSet<>()
                ).add(jpaSpecChangeLog.getValue()));
    }

    private void collectColumnsChanges(Set<VariableElement> fieldsElements,
                                       EntityConfig entityConfig,
                                       String jpaSpec,
                                       ChangeSet changeSet,
                                       Map<String, List<ChangeOp>> changeSetAtBeginningOperations,
                                       Map<String, List<ChangeOp>> changeSetCreateTableOperations,
                                       Map<String, List<ChangeOp>> changeSetAtEndOperations) {
        Stream.Builder<ChangeOp> addColumnOpsBuilder = Stream.builder();
        fieldsElements.forEach(fieldElement -> collectColumnsChanges(
                fieldElement,
                entityConfig.table(),
                jpaSpec,
                changeSet,
                addColumnOpsBuilder,
                changeSetAtBeginningOperations,
                changeSetAtEndOperations
        ));
        List<ChangeOp> columnOps = addColumnOpsBuilder.build()
                .collect(Collectors.toCollection(LinkedList::new));
        ChangeOp createTableOp = ChangeOp.builder()
                .operation("createTable")
                .attribute("schemaName", "${" + jpaSpec + "Schema}")
                .attribute("tableName", entityConfig.table())
                .childOperations(columnOps)
                .build();
        LinkedList<ChangeOp> tableOperations = new LinkedList<>();
        tableOperations.add(createTableOp);
        changeSetCreateTableOperations.put(changeSet.firstVersion(), tableOperations);
    }

    private void collectColumnsChanges(VariableElement fieldElement,
                                       String tableName,
                                       String jpaSpec,
                                       ChangeSet changeSet,
                                       Stream.Builder<ChangeOp> addColumnOpsBuilder,
                                       Map<String, List<ChangeOp>> changeSetAtBeginningOperations,
                                       Map<String, List<ChangeOp>> changeSetAtEndOperations) {
        if (tableMetadataUtil.isEmbeddable(fieldElement)) {
            if (processingEnv.getTypeUtils().asElement(fieldElement.asType()) instanceof TypeElement fieldTypeElement) {
                collectEmbeddableColumnOperations(
                        fieldElement,
                        fieldTypeElement,
                        tableName,
                        jpaSpec,
                        changeSet,
                        addColumnOpsBuilder,
                        changeSetAtBeginningOperations,
                        changeSetAtEndOperations
                );
            }
        } else {
            collectColumnOperations(
                    fieldElement,
                    tableName,
                    jpaSpec,
                    changeSet,
                    addColumnOpsBuilder,
                    changeSetAtBeginningOperations,
                    changeSetAtEndOperations
            );
        }
    }

    private void collectEmbeddableColumnOperations(VariableElement embeddableFieldElement,
                                                   TypeElement fieldTypeElement,
                                                   String tableName,
                                                   String jpaSpec,
                                                   ChangeSet changeSet,
                                                   Stream.Builder<ChangeOp> addColumnOpsBuilder,
                                                   Map<String, List<ChangeOp>> changeSetAtBeginningOperations,
                                                   Map<String, List<ChangeOp>> changeSetAtEndOperations) {
        Set<VariableElement> embeddableFieldElements = getDeclaredFields(fieldTypeElement);
        if (tableMetadataUtil.isEmbeddableIdentifier(embeddableFieldElement)) {
            String fkName = "fk_" + tableName;
            ChangeOp addUniqueConstraintOp = embeddableFieldElements.stream()
                    .map(tableMetadataUtil::getColumnName)
                    .collect(Collectors.collectingAndThen(
                            Collectors.joining(","),
                            columnNames -> ChangeOp.builder()
                                    .operation("addUniqueConstraint")
                                    .attribute("schemaName", "${schema}")
                                    .attribute("tableName", tableName)
                                    .attribute("columnNames", columnNames)
                                    .attribute("constraintName", fkName)
                                    .build()
                    ));
            changeSetAtEndOperations.computeIfAbsent(changeSet.firstVersion(), k -> new LinkedList<>())
                    .add(addUniqueConstraintOp);
        }
        embeddableFieldElements.forEach(fieldElement -> collectColumnOperations(
                fieldElement,
                tableName,
                jpaSpec,
                changeSet,
                addColumnOpsBuilder,
                changeSetAtBeginningOperations,
                changeSetAtEndOperations
        ));
    }

    private void collectColumnOperations(
            VariableElement fieldElement,
            String tableName,
            String jpaSpec,
            ChangeSet changeSet,
            Stream.Builder<ChangeOp> addColumnOpsBuilder,
            Map<String, List<ChangeOp>> changeSetAtBeginningOperations,
            Map<String, List<ChangeOp>> changeSetAtEndOperations) {
        String columnName = tableMetadataUtil.getColumnName(fieldElement);
        collectColumnOperations(
                fieldElement,
                changeSet,
                tableName,
                columnName,
                jpaSpec,
                addColumnOpsBuilder,
                changeSetAtBeginningOperations,
                changeSetAtEndOperations
        );
    }

    private void collectColumnOperations(VariableElement fieldElement,
                                         ChangeSet changeSet,
                                         String tableName,
                                         String columnName,
                                         String jpaSpec,
                                         Stream.Builder<ChangeOp> addColumnOpsBuilder,
                                         Map<String, List<ChangeOp>> changeSetAtBeginningOperations,
                                         Map<String, List<ChangeOp>> changeSetAtEndOperations) {
        resolveAddColumnOperations(
                fieldElement,
                tableName,
                columnName,
                fieldElement.getAnnotation(Column.class),
                addColumnOpsBuilder
        );
        if (tableMetadataUtil.isIdentifier(fieldElement) &&
                fieldElement.getAnnotation(SequenceGenerator.class) != null) {
            SequenceGenerator sequenceGenerator = fieldElement.getAnnotation(SequenceGenerator.class);
            String schema = sequenceGenerator.schema();
            if (StringUtils.isEmpty(schema)) {
                schema = "${" + jpaSpec + "Schema}";
            }
            ChangeOp createSequence = ChangeOp.builder()
                    .operation("createSequence")
                    .attribute("sequenceName", sequenceGenerator.name())
                    .attribute("schemaName", schema)
                    .attribute("startValue", String.valueOf(sequenceGenerator.initialValue()))
                    .attribute("incrementBy", String.valueOf(sequenceGenerator.allocationSize()))
                    .build();
            changeSetAtBeginningOperations.computeIfAbsent(changeSet.firstVersion(), k -> new LinkedList<>())
                    .add(createSequence);
        }
        Optional.ofNullable(fieldElement.getAnnotation(ColumnChanges.class))
                .ifPresent(changes -> collectColumnChangeOperations(
                        changes,
                        changeSet,
                        tableName,
                        columnName,
                        changeSetAtBeginningOperations,
                        changeSetAtEndOperations
                ));
    }

    private void resolveAddColumnOperations(VariableElement fieldElement,
                                            String tableName,
                                            String columnName,
                                            Column column,
                                            Stream.Builder<ChangeOp> addColumnOpsBuilder) {
        ChangeOp.Builder addColumnOp = ChangeOp.builder()
                .operation("column")
                .attribute("name", columnName)
                .attribute("type", tableMetadataUtil.assumeSqlType(fieldElement, column));
        ChangeOp.Builder constraints = null;
        if (tableMetadataUtil.isIdentifier(fieldElement)) {
            constraints = ChangeOp.builder()
                    .operation("constraints")
                    .attribute("primaryKey", String.valueOf(true))
                    .attribute("primaryKeyName", "pk_" + tableName);
        }
        if (column != null) {
            if (column.unique()) {
                if (constraints == null) {
                    constraints = ChangeOp.builder()
                            .operation("constraints");
                }
                constraints.attribute("unique", String.valueOf(true));
                constraints.attribute("uniqueConstraintName", "unique_" + tableName + '_' + columnName);
            }
            if (!column.nullable()) {
                if (constraints == null) {
                    constraints = ChangeOp.builder()
                            .operation("constraints");
                }
                constraints.attribute("nullable", String.valueOf(false));
                constraints.attribute("notNullConstraintName", "nonnull_" + tableName + '_' + columnName);
            }
        }
        if (fieldElement.getAnnotation(ForeignKey.class) != null) {
            if (constraints == null) {
                constraints = ChangeOp.builder()
                        .operation("constraints");
            }
            ForeignKey foreignKey = fieldElement.getAnnotation(ForeignKey.class);
            String foreginKeyName = foreignKey.name();
            if (StringUtils.isEmpty(foreginKeyName)) {
                foreginKeyName = "fk_" + tableName + '_' + columnName;
            }
            constraints.attribute("foreignKeyName", foreginKeyName);
            constraints.attribute("referencedTableName", foreignKey.referenceTable());
            constraints.attribute("referencedColumnNames", foreignKey.referenceColumn());
            if (StringUtils.isNotEmpty(foreignKey.referenceJpaSpec())) {
                constraints.attribute(
                        "referencedTableSchemaName",
                        "${" + foreignKey.referenceJpaSpec() + "Schema}"
                );
            }
        }
        if (constraints != null) {
            addColumnOp.childOperation(constraints.build());
        }
        addColumnOpsBuilder.add(addColumnOp.build());
    }

    private void collectColumnChangeOperations(ColumnChanges columnChanges,
                                               ChangeSet changeSet,
                                               String tableName,
                                               String columnName,
                                               Map<String, List<ChangeOp>> changeSetAtBeginningOperations,
                                               Map<String, List<ChangeOp>> changeSetAtEndOperations) {
        Arrays.stream(columnChanges.atBeginning()).forEach(columnChange -> collectAtBeginningOperations(
                columnChange,
                changeSet,
                tableName,
                columnName,
                changeSetAtBeginningOperations
        ));
        Arrays.stream(columnChanges.atEnd()).forEach(columnChange -> collectAtEndOperations(
                columnChange,
                changeSet,
                tableName,
                columnName,
                changeSetAtEndOperations
        ));
    }

    private void collectAtBeginningOperations(ColumnChange columnChange,
                                              ChangeSet changeSet,
                                              String tableName,
                                              String columnName,
                                              Map<String, List<ChangeOp>> changeSetAtBeginningOperations) {
        String version = columnChange.version().isEmpty() ? changeSet.firstVersion() : columnChange.version();
        List<ChangeOp> atBeginningOperations =
                changeSetAtBeginningOperations.computeIfAbsent(version, k -> new LinkedList<>());
        atBeginningOperations.addAll(buildOperations(columnChange.operation(), tableName, columnName, columnChange));
    }

    private void collectAtEndOperations(ColumnChange columnChange,
                                        ChangeSet changeSet,
                                        String tableName,
                                        String columnName,
                                        Map<String, List<ChangeOp>> changeSetAtEndOperations) {
        String version = columnChange.version().isEmpty() ? changeSet.firstVersion() : columnChange.version();
        List<ChangeOp> atEndOperations =
                changeSetAtEndOperations.computeIfAbsent(version, k -> new LinkedList<>());
        atEndOperations.addAll(buildOperations(columnChange.operation(), tableName, columnName, columnChange));
    }

    private List<ChangeOp> buildOperations(Operation operation,
                                           String tableName,
                                           String columnName,
                                           ColumnChange columnChange) {
        if (operation == Operation.ADD_NOT_NULL_CONSTRAINT) {
            ChangeOp addNotNull = ChangeOp.builder()
                    .operation("addNotNullConstraint")
                    .attribute("tableName", tableName)
                    .attribute("columnName", columnName)
                    .build();
            return Collections.singletonList(addNotNull);
        } else if (operation == Operation.DROP_DEFAULT_VALUE) {
            ChangeOp dropDefaultValue = ChangeOp.builder()
                    .operation("dropDefaultValue")
                    .attribute("table", tableName)
                    .attribute("column", columnName)
                    .build();
            return Collections.singletonList(dropDefaultValue);
        } else if (operation == Operation.DEFAULT_VALUE_CHANGE) {
            List<ChangeOp> operations = new LinkedList<>();
            ChangeOp dropDefaultValue = ChangeOp.builder()
                    .operation("dropDefaultValue")
                    .attribute("tableName", tableName)
                    .attribute("columnName", columnName)
                    .build();
            ChangeOp addDefaultValue = ChangeOp.builder()
                    .operation("addDefaultValue")
                    .attribute("tableName", tableName)
                    .attribute("columnName", columnName)
                    .build();
            operations.add(dropDefaultValue);
            operations.add(addDefaultValue);
            return operations;
        } else if (operation == Operation.DROP_NULL_CONSTRAINT) {
            ChangeOp dropNotNullConstraint = ChangeOp.builder()
                    .operation("dropNotNullConstraint")
                    .attribute("columnName", columnName)
                    .attribute("tableName", tableName)
                    .build();
            return Collections.singletonList(dropNotNullConstraint);
        } else if (operation == Operation.MODIFY_DATA_TYPE) {
            ChangeOp modifyDataType = ChangeOp.builder()
                    .operation("modifyDataType")
                    .attribute("columnName", columnName)
                    .attribute("tableName", tableName)
                    .attribute("newDataType", columnChange.parameters())
                    .build();
            return Collections.singletonList(modifyDataType);
        } else {
            return Collections.emptyList();
        }
    }

    private Set<VariableElement> getDeclaredFields(TypeElement typeElement) {
        return typeElement.getEnclosedElements()
                .stream()
                .filter(element -> element.getKind() == FIELD)
                .filter(VariableElement.class::isInstance)
                .map(VariableElement.class::cast)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<String> resolveVersions(Map<String, List<ChangeOp>> changeSetAtBeginningOperations,
                                         Map<String, List<ChangeOp>> changeSetOperations,
                                         Map<String, List<ChangeOp>> changeSetAtEndOperations) {
        Stream.Builder<String> versions = Stream.builder();
        changeSetAtBeginningOperations.keySet().forEach(versions::add);
        changeSetOperations.keySet().forEach(versions::add);
        changeSetAtEndOperations.keySet().forEach(versions::add);
        return versions.build()
                .distinct()
                .sorted(Comparator.comparing(s -> s))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private DefaultMapEntry<String, String> mapToJpaSpecDependency(ChangeSet.DependsOn dependsOn,
                                                                   JpaSpecMetadata changeSetJpaSpec,
                                                                   List<JpaSpecMetadata> jpaSpecsMetadata) {
        String jpaSpec = changeSetJpaSpec.jpaSpec();
        if (StringUtils.isBlank(jpaSpec)) {
            jpaSpec = changeSetJpaSpec.jpaSpec();
        }
        String referencedChangeLogId = resolveChangeLogIdByTable(
                jpaSpec,
                dependsOn.table(),
                dependsOn.version(),
                jpaSpecsMetadata
        );
        return new DefaultMapEntry<>(jpaSpec, referencedChangeLogId);
    }

    private DefaultMapEntry<String, String> mapToJpaSpecDependency(ForeignKey foreignKey,
                                                                   JpaSpecMetadata foreignKeyJpaSpec,
                                                                   List<JpaSpecMetadata> jpaSpecsMetadata) {
        String jpaSpec = foreignKey.referenceJpaSpec();
        String referenceChangeLogId = foreignKey.referenceChangeLogId();
        if (StringUtils.isBlank(jpaSpec)) {
            jpaSpec = foreignKeyJpaSpec.jpaSpec();
        }
        if (StringUtils.isBlank(referenceChangeLogId)) {
            referenceChangeLogId = resolveReferencedChangeLogId(
                    jpaSpec,
                    foreignKey.referenceTable(),
                    foreignKey.referenceColumn(),
                    jpaSpecsMetadata
            );
        }
        return new DefaultMapEntry<>(jpaSpec, referenceChangeLogId);
    }

    private String resolveChangeLogIdByTable(String jpaSpec,
                                             String table,
                                             String version,
                                             List<JpaSpecMetadata> jpaSpecsMetadata) {
        return jpaSpecsMetadata.stream()
                .filter(metadata -> StringUtils.equals(metadata.jpaSpec(), jpaSpec))
                .findFirst()
                .flatMap(metadata -> resolveChangeLogIdByTable(
                        metadata,
                        table,
                        version
                )).orElse(null);
    }

    private Optional<String> resolveChangeLogIdByTable(JpaSpecMetadata referencedJpaSpecMetadata,
                                                       String table,
                                                       String version) {
        return referencedJpaSpecMetadata.entitiesConfig()
                .stream()
                .filter(entityConfig -> StringUtils.equals(entityConfig.table(), table))
                .findFirst()
                .map(entityConfig -> resolveChangeLogIdByTable(entityConfig, version));
    }

    private String resolveChangeLogIdByTable(EntityConfig entityConfig, String version) {
        return generateId(entityConfig.entity().getAnnotation(ChangeSet.class), version, entityConfig.table());
    }

    private String resolveReferencedChangeLogId(String referencedJpaSpec,
                                                String referencedTable,
                                                String referencedColumns,
                                                List<JpaSpecMetadata> jpaSpecsMetadata) {
        return jpaSpecsMetadata.stream()
                .filter(metadata -> StringUtils.equals(metadata.jpaSpec(), referencedJpaSpec))
                .findFirst()
                .flatMap(metadata -> resolveReferencedChangeLogId(
                        metadata,
                        referencedTable,
                        referencedColumns
                )).orElse(null);
    }

    private Optional<String> resolveReferencedChangeLogId(JpaSpecMetadata referencedJpaSpecMetadata,
                                                          String referencedTable,
                                                          String referencedColumns) {
        return referencedJpaSpecMetadata.entitiesConfig().stream()
                .filter(entityConfig -> StringUtils.equals(entityConfig.table(), referencedTable))
                .findFirst()
                .flatMap(entityConfig -> resolveReferencedChangeLogId(
                        entityConfig,
                        referencedColumns
                ));
    }

    private Optional<String> resolveReferencedChangeLogId(EntityConfig entityConfig,
                                                          String referencedColumns) {
        TypeElement entity = entityConfig.entity();
        Set<VariableElement> entityFields = getDeclaredFields(entity);
        return entityFields.stream()
                .map(field -> new DefaultMapEntry<>(tableMetadataUtil.getColumnName(field), field))
                .filter(columnField ->
                        StringUtils.equals(columnField.getKey(), referencedColumns)
                ).findFirst()
                .map(DefaultMapEntry::getValue)
                .map(columnField -> resolveReferencedChangeLogId(
                        columnField,
                        entityConfig
                ));
    }

    private String resolveReferencedChangeLogId(VariableElement field,
                                                EntityConfig entityConfig) {
        TypeElement entity = entityConfig.entity();
        ChangeSet changeSet = entity.getAnnotation(ChangeSet.class);
        String version = version = changeSet.firstVersion();
        if (StringUtils.isBlank(version) && field.getAnnotation(ColumnChanges.class) != null) {

        }
        return generateId(changeSet, version, entityConfig.table());
    }

    private String generateId(ChangeSet changeSet, String version, String tableName) {
        String id = changeSet.id();
        id = id.replace("{version}", version);
        id = id.replace("{table}", tableName);
        return id;
    }

}
