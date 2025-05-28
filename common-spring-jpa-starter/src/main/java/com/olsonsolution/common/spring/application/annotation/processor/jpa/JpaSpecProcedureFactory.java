package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.olsonsolution.common.spring.application.annotation.migration.*;
import jakarta.persistence.Column;
import jakarta.persistence.SequenceGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.olsonsolution.common.spring.application.annotation.processor.jpa.JpaSpecMetadata.FIRST_VERSION;

@RequiredArgsConstructor
class JpaSpecProcedureFactory {

    private final ChangeLogOrderer changeLogOrderer;

    private final LiquibaseUtils liquibaseUtils;

    private final JpaEntityUtil jpaEntityUtil;

    private final JpaSpecAnnotationUtils jpaSpecAnnotationUtils;

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
        return changeSets.build().collect(Collectors.collectingAndThen(
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
        Map<String, List<ChangeOp>> changeSetOps = new LinkedHashMap<>();
        TypeElement entityType = entityConfig.entity();
        String jpaSpec = jpaSpecMetadata.jpaSpec();
        String table = entityConfig.table();
        Map<String, VariableElement> columnMappings = jpaEntityUtil.obtainColumnMappings(entityType);
        if (entityType.getAnnotation(ColumnChanges.class) != null) {
            ColumnChanges columnChanges = entityType.getAnnotation(ColumnChanges.class);
            collectEntityChangesOps(columnChanges, jpaSpec, table, columnMappings, changeSetOps);
        }
        collectSequenceGenerators(columnMappings, jpaSpec, changeSetOps);
        collectColumnsChanges(columnMappings, entityType, table, jpaSpec, changeSetOps);
        changeSetOps.forEach((version, ops) -> collectChangeSetOps(
                version, ops, jpaSpecMetadata, jpaSpecsMetadata, table, changeSet, columnMappings, changeSets
        ));
    }

    private void collectEntityChangesOps(ColumnChanges columnChanges, String jpaSpec, String table,
                                         Map<String, VariableElement> columnMappings,
                                         Map<String, List<ChangeOp>> changeSetOps) {
        for (ColumnChange columnChange : columnChanges.value()) {
            collectEntityChangesOps(columnChange, jpaSpec, table, columnMappings, changeSetOps);
        }
    }

    private void collectEntityChangesOps(ColumnChange columnChange,
                                         String jpaSpec, String table,
                                         Map<String, VariableElement> columnMappings,
                                         Map<String, List<ChangeOp>> changeSetOps) {
        VariableElement entityField;
        String column = columnChange.column();
        if (StringUtils.isEmpty(column) || !columnMappings.containsKey(column)) {
            return;
        } else {
            entityField = columnMappings.get(column);
        }
        collectOperations(columnChange, null, entityField, jpaSpec, table, columnChange.column(), changeSetOps);
    }

    private void collectSequenceGenerators(Map<String, VariableElement> columnMappings, String jpaSpec,
                                           Map<String, List<ChangeOp>> changeSetOps) {
        columnMappings.entrySet()
                .stream()
                .filter(columnMapping ->
                        columnMapping.getValue().getAnnotation(SequenceGenerator.class) != null)
                .forEach(columnMapping -> collectSequenceGenerator(
                        columnMapping, jpaSpec, changeSetOps
                ));
    }

    private void collectSequenceGenerator(Map.Entry<String, VariableElement> columnMapping, String jpaSpec,
                                          Map<String, List<ChangeOp>> changeSetOps) {
        SequenceGenerator sequenceGenerator = columnMapping.getValue().getAnnotation(SequenceGenerator.class);
        ChangeOp createSequence = liquibaseUtils.parseSequenceGenerator(jpaSpec, sequenceGenerator);
        changeSetOps.computeIfAbsent(FIRST_VERSION, k -> new LinkedList<>()).add(createSequence);
    }

    private void collectChangeSetOps(String version, List<ChangeOp> operations,
                                     JpaSpecMetadata jpaSpecMetadata, List<JpaSpecMetadata> jpaSpecsMetadata,
                                     String table, ChangeSet changeSet,
                                     Map<String, VariableElement> columnMappings,
                                     Stream.Builder<ChangeSetOp> changeSets) {
        Map<String, Set<String>> dependsOn =
                collectDependsOn(changeSet, columnMappings, jpaSpecMetadata, jpaSpecsMetadata);
        ChangeSetOp changeSetOp = ChangeSetOp.builder()
                .table(table)
                .version(version)
                .id(jpaSpecAnnotationUtils.generateId(changeSet, version, table))
                .operations(operations)
                .dependsOn(dependsOn)
                .build();
        changeSets.add(changeSetOp);
    }

    private Map<String, Set<String>> collectDependsOn(ChangeSet changeSet,
                                                      Map<String, VariableElement> columnMappings,
                                                      JpaSpecMetadata jpaSpecMetadata,
                                                      List<JpaSpecMetadata> jpaSpecsMetadata) {
        Map<String, Set<String>> dependencies = new HashMap<>();
        collectForeignKeyDependencies(columnMappings, jpaSpecMetadata, jpaSpecsMetadata, dependencies);
        collectChangeSetDependencies(changeSet, jpaSpecMetadata, jpaSpecsMetadata, dependencies);
        return dependencies;
    }

    private void collectForeignKeyDependencies(Map<String, VariableElement> columnMappings,
                                               JpaSpecMetadata jpaSpecMetadata,
                                               List<JpaSpecMetadata> jpaSpecsMetadata,
                                               Map<String, Set<String>> dependencies) {
        for (VariableElement mappedField : columnMappings.values()) {
            collectForeignKeyDependencies(mappedField, jpaSpecMetadata, jpaSpecsMetadata, dependencies);
        }
    }

    private void collectForeignKeyDependencies(VariableElement field,
                                               JpaSpecMetadata jpaSpecMetadata,
                                               List<JpaSpecMetadata> jpaSpecsMetadata,
                                               Map<String, Set<String>> dependencies) {
        if (field.getAnnotation(ForeignKey.class) != null) {
            ForeignKey foreignKey = field.getAnnotation(ForeignKey.class);
            Map.Entry<String, String> jpaSpecDependency =
                    mapToJpaSpecDependency(foreignKey, jpaSpecMetadata, jpaSpecsMetadata);
            dependencies.computeIfAbsent(jpaSpecDependency.getKey(), k -> new HashSet<>())
                    .add(jpaSpecDependency.getValue());
        }
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

    private void collectColumnsChanges(Map<String, VariableElement> columnMappings,
                                       TypeElement entityType, String table, String jpaSpec,
                                       Map<String, List<ChangeOp>> changeSetOps) {
        Stream.Builder<ChangeOp> addColumnOpsBuilder = Stream.builder();
        columnMappings.forEach((column, entityField) -> collectColumnOperations(
                column, entityField, entityType,
                table, jpaSpec,
                addColumnOpsBuilder,
                changeSetOps
        ));
        List<ChangeOp> columnOps = addColumnOpsBuilder.build().collect(Collectors.toCollection(LinkedList::new));
        ChangeOp createTableOp = liquibaseUtils.buildCreateTable(jpaSpec, table, columnOps);
        changeSetOps.computeIfAbsent(FIRST_VERSION, v -> new LinkedList<>()).add(createTableOp);
    }

    private void collectColumnOperations(String column, VariableElement entityField, TypeElement entityElement,
                                         String tableName, String jpaSpec, Stream.Builder<ChangeOp> addColumnOpsBuilder,
                                         Map<String, List<ChangeOp>> changeSetOps) {
        String version = getAddColumnVersion(entityElement, entityField, column);
        Column columnAnno = entityField.getAnnotation(Column.class);
        boolean isIdentifier = jpaEntityUtil.isIdentifier(entityField);
        if (StringUtils.equals(version, FIRST_VERSION)) {
            ChangeOp columnOp =
                    liquibaseUtils.buildColumnOp(entityField, jpaSpec, tableName, column, columnAnno, isIdentifier);
            addColumnOpsBuilder.accept(columnOp);
        }
        Optional.ofNullable(entityField.getAnnotation(ColumnChanges.class))
                .ifPresent(changes -> collectColumnChangeOperations(
                        changes, columnAnno, entityField,
                        jpaSpec, tableName, column,
                        changeSetOps
                ));
    }

    private void collectColumnChangeOperations(ColumnChanges columnChanges,
                                               Column column,
                                               VariableElement entityField,
                                               String jpaSpec, String tableName, String columnName,
                                               Map<String, List<ChangeOp>> changeSetAtEndOperations) {
        for (ColumnChange columnChange : columnChanges.value()) {
            collectOperations(
                    columnChange, column, entityField, jpaSpec, tableName, columnName, changeSetAtEndOperations);
        }
    }

    private void collectOperations(ColumnChange columnChange, Column column, VariableElement entityField,
                                   String jpaSpec, String tableName, String columnName,
                                   Map<String, List<ChangeOp>> changeSetOps) {
        String version = columnChange.ver().isEmpty() ? FIRST_VERSION : columnChange.ver();
        List<ChangeOp> atEndOperations = changeSetOps.computeIfAbsent(version, k -> new LinkedList<>());
        atEndOperations.addAll(liquibaseUtils.buildChangeOps(
                columnChange.op(), jpaSpec, tableName, columnName,
                columnChange, column, entityField)
        );
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
                    jpaSpec, foreignKey.referenceTable(), foreignKey.referenceColumn(), jpaSpecsMetadata
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
                .flatMap(metadata -> resolveChangeLogIdByTable(metadata, table, version))
                .orElse(null);
    }

    private Optional<String> resolveChangeLogIdByTable(JpaSpecMetadata referencedJpaSpecMetadata,
                                                       String table,
                                                       String version) {
        return referencedJpaSpecMetadata.entitiesConfig()
                .stream()
                .filter(entityConfig -> StringUtils.equals(entityConfig.table(), table))
                .findFirst()
                .map(entityConfig -> jpaSpecAnnotationUtils.generateId(
                        entityConfig.entity().getAnnotation(ChangeSet.class),
                        entityConfig.table(),
                        version
                ));
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
        Map<String, VariableElement> entityFields = jpaEntityUtil.obtainColumnMappings(entityConfig.entity());
        return entityFields.entrySet()
                .stream()
                .filter(columnField ->
                        StringUtils.equals(columnField.getKey(), referencedColumns)
                ).findFirst()
                .map(mapping -> resolveReferencedChangeLogId(mapping, entityConfig));
    }

    private String resolveReferencedChangeLogId(Map.Entry<String, VariableElement> columnField,
                                                EntityConfig entityConfig) {
        TypeElement entityElement = entityConfig.entity();
        ChangeSet changeSet = entityElement.getAnnotation(ChangeSet.class);
        String version = getAddColumnVersion(entityElement, columnField.getValue(), columnField.getKey());
        return jpaSpecAnnotationUtils.generateId(changeSet, version, entityConfig.table());
    }

    private String getAddColumnVersion(TypeElement entityElement, VariableElement fieldElement, String column) {
        return jpaSpecAnnotationUtils.listColumnChangesForField(entityElement, fieldElement, column)
                .stream()
                .filter(columnChange -> columnChange.op() == Operation.ADD_COLUMN)
                .map(ColumnChange::ver)
                .min(Comparator.naturalOrder())
                .orElse(FIRST_VERSION);
    }

    private List<ChangeOp> addUniqueConstraint(String columnNames, String jpaSpec, String tableName) {
        String constraintName = "id_" + tableName;
        Param columnNamesParam = crateParameter("columnNames", columnNames);
        Param constraintNameParam = crateParameter("constraintName", constraintName);
        ColumnChange addUniqueConstraint = new ColumnChange() {
            @Override
            public String column() {
                return "";
            }

            @Override
            public Operation op() {
                return Operation.ADD_UNIQUE_CONSTRAINT;
            }

            @Override
            public Param[] params() {
                return new Param[]{columnNamesParam, constraintNameParam};
            }

            @Override
            public String ver() {
                return FIRST_VERSION;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return ColumnChange.class;
            }
        };
        return liquibaseUtils.buildChangeOps(
                Operation.ADD_UNIQUE_CONSTRAINT, jpaSpec, tableName, "",
                addUniqueConstraint, null, null
        );
    }

    private Param crateParameter(String name, String value) {
        return new Param() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Param.class;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String value() {
                return value;
            }
        };
    }

}
