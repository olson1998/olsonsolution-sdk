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
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.olsonsolution.common.spring.application.annotation.processor.jpa.JpaSpecMetadata.FIRST_VERSION;
import static javax.lang.model.element.ElementKind.FIELD;

@RequiredArgsConstructor
class JpaSpecProcedureFactory {

    private final ChangeLogOrderer changeLogOrderer;

    private final ProcessingEnvironment processingEnv;

    private final TableMetadataUtil tableMetadataUtil;

    /**
     * Creates and returns a {@link JpaSpecExecPlan} based on the provided list of JPA specification metadata.
     * The method processes the metadata to generate procedures, sorts the specification names,
     * and determines the dependency order for change logs.
     *
     * @param jpaSpecsMetadata the list of {@link JpaSpecMetadata} instances representing JPA specification metadata that needs to be processed
     * @return a {@link JpaSpecExecPlan} containing the generated procedures, sorted specification names, and change log execution order
     */
    JpaSpecExecPlan fabricate(List<JpaSpecMetadata> jpaSpecsMetadata) {
        List<JpaSpecProcedure> procedures = jpaSpecsMetadata.stream()
                .map(jpaSpecMetadata -> createProcedure(jpaSpecMetadata, jpaSpecsMetadata))
                .toList();
        LinkedHashSet<String> jpaSpecNames = procedures.stream()
                .map(JpaSpecProcedure::metadata)
                .map(JpaSpecMetadata::jpaSpec)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        LinkedHashSet<String> changeLogOrder = changeLogOrderer.sortChangeLogs(procedures);
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
        Map<String, List<ChangeOp>> changeSetAtBeginningOperations = new LinkedHashMap<>();
        Map<String, List<ChangeOp>> changeSetCreateTableOperations = new LinkedHashMap<>();
        Map<String, List<ChangeOp>> changeSetAtEndOperations = new LinkedHashMap<>();
        Map<String, LinkedList<ChangeOp>> changeSetOperations = new LinkedHashMap<>();
        Set<VariableElement> fields = getDeclaredFields(entityConfig.entity());
        collectColumnsChanges(
                fields,
                entityConfig,
                jpaSpecMetadata.jpaSpec(),
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
                                       Map<String, List<ChangeOp>> changeSetAtBeginningOperations,
                                       Map<String, List<ChangeOp>> changeSetCreateTableOperations,
                                       Map<String, List<ChangeOp>> changeSetAtEndOperations) {
        Stream.Builder<ChangeOp> addColumnOpsBuilder = Stream.builder();
        fieldsElements.forEach(fieldElement -> collectColumnsChanges(
                fieldElement,
                entityConfig.table(),
                jpaSpec,
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
        changeSetCreateTableOperations.put(FIRST_VERSION, tableOperations);
    }

    private void collectColumnsChanges(VariableElement fieldElement,
                                       String tableName,
                                       String jpaSpec,
                                       Stream.Builder<ChangeOp> addColumnOpsBuilder,
                                       Map<String, List<ChangeOp>> changeSetAtBeginningOperations,
                                       Map<String, List<ChangeOp>> changeSetAtEndOperations) {
        if (tableMetadataUtil.isEmbeddable(fieldElement)) {
            if (processingEnv.getTypeUtils().asElement(fieldElement.asType()) instanceof TypeElement fieldTypeElement) {
                collectEmbeddableColumnOperations(
                        fieldElement,
                        fieldTypeElement,
                        jpaSpec,
                        tableName,
                        addColumnOpsBuilder,
                        changeSetAtBeginningOperations,
                        changeSetAtEndOperations
                );
            }
        } else {
            collectColumnOperations(
                    fieldElement,
                    jpaSpec,
                    tableName,
                    addColumnOpsBuilder,
                    changeSetAtBeginningOperations,
                    changeSetAtEndOperations
            );
        }
    }

    private void collectEmbeddableColumnOperations(VariableElement embeddableFieldElement,
                                                   TypeElement fieldTypeElement,
                                                   String jpaSpec,
                                                   String tableName,
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
                                    .attribute("schemaName", "${" + jpaSpec + "Schema}")
                                    .attribute("tableName", tableName)
                                    .attribute("columnNames", columnNames)
                                    .attribute("constraintName", fkName)
                                    .build()
                    ));
            changeSetAtEndOperations.computeIfAbsent(FIRST_VERSION, k -> new LinkedList<>())
                    .add(addUniqueConstraintOp);
        }
        embeddableFieldElements.forEach(fieldElement -> collectColumnOperations(
                fieldElement,
                jpaSpec,
                tableName,
                addColumnOpsBuilder,
                changeSetAtBeginningOperations,
                changeSetAtEndOperations
        ));
    }

    private void collectColumnOperations(
            VariableElement fieldElement,
            String jpaSpec,
            String tableName,
            Stream.Builder<ChangeOp> addColumnOpsBuilder,
            Map<String, List<ChangeOp>> changeSetAtBeginningOperations,
            Map<String, List<ChangeOp>> changeSetAtEndOperations) {
        String columnName = tableMetadataUtil.getColumnName(fieldElement);
        collectColumnOperations(
                fieldElement,
                jpaSpec,
                tableName,
                columnName,
                addColumnOpsBuilder,
                changeSetAtBeginningOperations,
                changeSetAtEndOperations
        );
    }

    private void collectColumnOperations(VariableElement fieldElement,
                                         String jpaSpec,
                                         String tableName,
                                         String columnName,
                                         Stream.Builder<ChangeOp> addColumnOpsBuilder,
                                         Map<String, List<ChangeOp>> changeSetAtBeginningOperations,
                                         Map<String, List<ChangeOp>> changeSetAtEndOperations) {
        resolveAddColumnOperations(
                fieldElement,
                jpaSpec,
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
            changeSetAtBeginningOperations.computeIfAbsent(FIRST_VERSION, k -> new LinkedList<>())
                    .add(createSequence);
        }
        Optional.ofNullable(fieldElement.getAnnotation(ColumnChanges.class))
                .ifPresent(changes -> collectColumnChangeOperations(
                        changes,
                        jpaSpec,
                        tableName,
                        columnName,
                        changeSetAtBeginningOperations,
                        changeSetAtEndOperations
                ));
    }

    private void resolveAddColumnOperations(VariableElement fieldElement,
                                            String jpaSpec,
                                            String tableName,
                                            String columnName,
                                            Column column,
                                            Stream.Builder<ChangeOp> addColumnOpsBuilder) {
        ChangeOp.Builder addColumnOp = ChangeOp.builder()
                .operation("column")
                .attribute("name", columnName)
                .attribute("type", tableMetadataUtil.getSqlType(fieldElement, column));
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
            String schemaVariable = "${" + jpaSpec + "Schema}";
            if (StringUtils.isNotEmpty(foreignKey.referenceJpaSpec())) {
                schemaVariable = "${" + foreignKey.referenceJpaSpec() + "Schema}";
            }
            constraints.attribute("referencedTableSchemaName", schemaVariable);
        }
        if (constraints != null) {
            addColumnOp.childOperation(constraints.build());
        }
        addColumnOpsBuilder.add(addColumnOp.build());
    }

    private void collectColumnChangeOperations(ColumnChanges columnChanges,
                                               String jpaSpec,
                                               String tableName,
                                               String columnName,
                                               Map<String, List<ChangeOp>> changeSetAtBeginningOperations,
                                               Map<String, List<ChangeOp>> changeSetAtEndOperations) {
        Arrays.stream(columnChanges.atBeginning()).forEach(columnChange -> collectAtBeginningOperations(
                columnChange,
                jpaSpec,
                tableName,
                columnName,
                changeSetAtBeginningOperations
        ));
        Arrays.stream(columnChanges.atEnd()).forEach(columnChange -> collectAtEndOperations(
                columnChange,
                jpaSpec,
                tableName,
                columnName,
                changeSetAtEndOperations
        ));
    }

    private void collectAtBeginningOperations(ColumnChange columnChange,
                                              String jpaSpec,
                                              String tableName,
                                              String columnName,
                                              Map<String, List<ChangeOp>> changeSetAtBeginningOperations) {
        String version = columnChange.version().isEmpty() ? FIRST_VERSION : columnChange.version();
        List<ChangeOp> atBeginningOperations =
                changeSetAtBeginningOperations.computeIfAbsent(version, k -> new LinkedList<>());
        atBeginningOperations.addAll(
                buildOperations(columnChange.operation(),
                        jpaSpec, tableName, columnName, columnChange)
        );
    }

    private void collectAtEndOperations(ColumnChange columnChange,
                                        String jpaSpec,
                                        String tableName,
                                        String columnName,
                                        Map<String, List<ChangeOp>> changeSetAtEndOperations) {
        String version = columnChange.version().isEmpty() ? FIRST_VERSION : columnChange.version();
        List<ChangeOp> atEndOperations =
                changeSetAtEndOperations.computeIfAbsent(version, k -> new LinkedList<>());
        atEndOperations.addAll(buildOperations(columnChange.operation(), jpaSpec, tableName, columnName, columnChange));
    }

    private List<ChangeOp> buildOperations(Operation operation,
                                           String jpaSpec,
                                           String tableName,
                                           String columnName,
                                           ColumnChange columnChange) {
        if (operation == Operation.ADD_NOT_NULL_CONSTRAINT) {
            ChangeOp addNotNull = ChangeOp.builder()
                    .operation("addNotNullConstraint")
                    .attribute("tableName", tableName)
                    .attribute("columnName", columnName)
                    .attribute("schemaName", "${" + jpaSpec + "Schema}")
                    .attribute("columnDataType", getParameter(columnChange, "columnDataType"))
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
                    .attribute("schemaName", "${" + jpaSpec + "Schema}")
                    .build();
            ChangeOp addDefaultValue = ChangeOp.builder()
                    .operation("addDefaultValue")
                    .attribute("tableName", tableName)
                    .attribute("columnName", columnName)
                    .attribute("schemaName", "${" + jpaSpec + "Schema}")
                    .build();
            operations.add(dropDefaultValue);
            operations.add(addDefaultValue);
            return operations;
        } else if (operation == Operation.DROP_NULL_CONSTRAINT) {
            ChangeOp dropNotNullConstraint = ChangeOp.builder()
                    .operation("dropNotNullConstraint")
                    .attribute("columnName", columnName)
                    .attribute("tableName", tableName)
                    .attribute("schemaName", "${" + jpaSpec + "Schema}")
                    .build();
            return Collections.singletonList(dropNotNullConstraint);
        } else if (operation == Operation.MODIFY_DATA_TYPE) {
            ChangeOp modifyDataType = ChangeOp.builder()
                    .operation("modifyDataType")
                    .attribute("columnName", columnName)
                    .attribute("tableName", tableName)
                    .attribute("schemaName", "${" + jpaSpec + "Schema}")
                    .attribute("newDataType", getParameter(columnChange, "newDataType"))
                    .build();
            return Collections.singletonList(modifyDataType);
        } else {
            return Collections.emptyList();
        }
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
        String version = "1.0.0";
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

    private String getParameter(ColumnChange columnChange, String parameterName) {
        return Arrays.stream(columnChange.parameters())
                .filter(parameter -> StringUtils.equals(parameter.name(), parameterName))
                .findFirst()
                .map(ColumnChange.Parameter::value)
                .orElseThrow();
    }

    private Set<VariableElement> getDeclaredFields(TypeElement typeElement) {
        Stream.Builder<VariableElement> fields = Stream.builder();
        collectDeclaredFields(typeElement, fields);
        return fields.build().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void collectDeclaredFields(TypeElement typeElement, Stream.Builder<VariableElement> fields) {
        typeElement.getEnclosedElements()
                .stream()
                .filter(element -> element.getKind() == FIELD)
                .filter(VariableElement.class::isInstance)
                .map(VariableElement.class::cast)
                .forEach(fields::add);
        TypeMirror superClassMirror = typeElement.getSuperclass();
        TypeElement objectClassElement = processingEnv.getElementUtils().getTypeElement("java.lang.Object");
        TypeMirror objectClassMirror = objectClassElement.asType();
        Types typeUtils = processingEnv.getTypeUtils();
        boolean isObjectClass = typeUtils.isSameType(
                typeUtils.erasure(superClassMirror),
                typeUtils.erasure(objectClassMirror)
        );
        if (!isObjectClass && superClassMirror instanceof DeclaredType &&
                typeUtils.asElement(superClassMirror) instanceof TypeElement superClassElement) {
            collectDeclaredFields(superClassElement, fields);
        }
    }

}
