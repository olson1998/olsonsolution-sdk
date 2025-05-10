package com.olsonsolution.common.spring.application.annotation.processor.migration;

import com.olsonsolution.common.spring.application.annotation.migration.*;
import jakarta.persistence.Column;
import jakarta.persistence.SequenceGenerator;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.w3c.dom.Document;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.olsonsolution.common.spring.application.annotation.processor.migration.ConstraintMetadata.Type.*;
import static java.util.Map.entry;
import static javax.lang.model.element.ElementKind.FIELD;
import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static javax.xml.transform.OutputKeys.*;

public class ChangeSetAnnotationProcessor {

    private final ProcessingEnvironment processingEnv;
    private final TableMetadataUtil tableMetadataUtil;

    public ChangeSetAnnotationProcessor(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.tableMetadataUtil = new TableMetadataUtil(processingEnv);
    }

    public void process(Map<String, List<TypeElement>> jpaSpecEntities) {
        try {
            Map<String, List<ChangeSetMetadata>> jpaSpecChangeSetMetadata = collectChangeSetMetadata(jpaSpecEntities);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Jpa Spec Change Sets: " + jpaSpecChangeSetMetadata);
            jpaSpecChangeSetMetadata.forEach((this::processForJpaSpec));
        } catch (Exception e) {
            String msg = e.getClass().getCanonicalName() + "\n" + ExceptionUtils.getStackTrace(e);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
        }
    }

    private void processForJpaSpec(String jpaSpec, List<ChangeSetMetadata> changeSetMetadata) {
        try {
            Map<ChangeSetMetadata, Document> changeSetChangeLogs =
                    ChangeLogGenerator.generateChangeLogs(changeSetMetadata);
            Map.Entry<String, Document> masterChangeLog =
                    ChangeLogGenerator.generateMasterChangeLog(changeSetChangeLogs);
            for (Map.Entry<ChangeSetMetadata, Document> changeSetChangeLog : changeSetChangeLogs.entrySet()) {
                ChangeSetMetadata metadata = changeSetChangeLog.getKey();
                String changeLogLocation = "/db/changelog/" + jpaSpec + '/' + metadata.changelogName();
                createChangeLogXml(changeLogLocation, changeSetChangeLog.getValue());
            }
            if (!changeSetChangeLogs.isEmpty()) {
                createChangeLogXml(masterChangeLog.getKey(), masterChangeLog.getValue());
            }
        } catch (ParserConfigurationException | IOException | TransformerException e) {
            String ThrowableMsg = ExceptionUtils.getStackTrace(e);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage() + "\n" + ThrowableMsg);
        }
    }

    private Map<String, List<ChangeSetMetadata>> collectChangeSetMetadata(
            Map<String, List<TypeElement>> jpaSpecEntities) {
        Map<TypeElement, ChangeSet> changeSetEntities = collectJpaEntities(jpaSpecEntities);
        Map<String, List<ChangeSetMetadata>> jpaSpecChangeSetMetadata = new HashMap<>();
        Map<String, Map<String, ChangeSet>> jpaSpecTableChangeSet = new HashMap<>();
        changeSetEntities.forEach((typeElement, changeSet) -> collectChangesetMetadata(
                typeElement,
                changeSet,
                jpaSpecEntities,
                jpaSpecChangeSetMetadata,
                jpaSpecTableChangeSet
        ));
        Map<String, List<ChangeSetMetadata>> orderedJpaSpecChangeSetMetadata =
                new LinkedHashMap<>(jpaSpecChangeSetMetadata.size());
        List<String> orderedJpaSpec = jpaSpecChangeSetMetadata.keySet()
                .stream()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toCollection(LinkedList::new));
        orderedJpaSpec.forEach(jpaSpec -> {
            List<ChangeSetMetadata> changeSetMetadata = ListUtils.emptyIfNull(jpaSpecChangeSetMetadata.get(jpaSpec));
            Map<String, ChangeSet> tableChangeSets = MapUtils.emptyIfNull(jpaSpecTableChangeSet.get(jpaSpec));
            ChangeSetOrderer orderer = buildOrderer(tableChangeSets, changeSetMetadata);
            List<ChangeSetMetadata> orderedChangeSetMetadata = changeSetMetadata.stream()
                    .sorted(orderer)
                    .collect(Collectors.toCollection(LinkedList::new));
            orderedJpaSpecChangeSetMetadata.put(jpaSpec, orderedChangeSetMetadata);
        });
        return orderedJpaSpecChangeSetMetadata;
    }

    private void collectChangesetMetadata(TypeElement typeElement,
                                          ChangeSet changeSet,
                                          Map<String, List<TypeElement>> jpaSpecsEntities,
                                          Map<String, List<ChangeSetMetadata>> jpaSpecChangeSetMetadata,
                                          Map<String, Map<String, ChangeSet>> jpaSpecTableChangeSet) {
        String jpaSpec = jpaSpecsEntities.entrySet()
                .stream()
                .filter(jpaSpecEntities -> jpaSpecEntities.getValue().contains(typeElement))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElseThrow();
        String table = tableMetadataUtil.resolveTableName(typeElement);
        jpaSpecChangeSetMetadata.computeIfAbsent(jpaSpec, s -> new ArrayList<>());
        jpaSpecTableChangeSet.computeIfAbsent(jpaSpec, s -> new HashMap<>())
                .put(jpaSpec, changeSet);
        collectChangeSetMetadata(typeElement, table, changeSet, jpaSpecChangeSetMetadata);
    }

    private void createChangeLogXml(String changeLogLocation,
                                    Document changeLogXml) throws IOException, TransformerException {
        Filer filer = processingEnv.getFiler();
        FileObject changeLogFile = filer.createResource(
                CLASS_OUTPUT,
                "",
                changeLogLocation
        );
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(INDENT, "yes");
        transformer.setOutputProperty(OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(METHOD, "xml");
        transformer.setOutputProperty(ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        try (Writer writer = changeLogFile.openWriter()) {
            StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(changeLogXml), new StreamResult(stringWriter));
            writer.write(stringWriter.toString());
        }
    }

    private void collectChangeSetMetadata(TypeElement typeElement,
                                          String table,
                                          ChangeSet changeSet,
                                          Map<String, List<ChangeSetMetadata>> jpaSpecChangeSetMetadata) {
        Map<String, List<ChangeSetOperation>> changeSetAtBeginningOperations = new HashMap<>();
        Map<String, List<ChangeSetOperation>> changeSetCreateTableOperations = new HashMap<>();
        Map<String, List<ChangeSetOperation>> changeSetAtEndOperations = new HashMap<>();
        Map<String, List<ChangeSetOperation>> changeSetOperations = new LinkedHashMap<>();
        Set<VariableElement> fields = getDeclaredFields(typeElement);
        collectColumnsChanges(
                fields,
                table,
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
        changeSetOperations.forEach((version, ops) -> collectChangeSetMetadata(
                version,
                ops,
                changeSet,
                table,
                typeElement,
                fields,
                jpaSpecChangeSetMetadata
        ));
    }

    private void collectChangeSetMetadata(String version,
                                          List<ChangeSetOperation> changeSetOperations,
                                          ChangeSet changeSet,
                                          String table,
                                          TypeElement typeElement,
                                          Set<VariableElement> fields,
                                          Map<String, List<ChangeSetMetadata>> jpaSpecChangeSetMetadata) {
        String changeLogName = generateChangeLogName(changeSet, version, table);
        Set<String> dependsOn = collectDependsOn(changeSet, fields);
        ChangeSetMetadata changeSetMetadata = new ChangeSetMetadata(
                typeElement,
                table,
                version,
                changeSet.path(),
                changeLogName,
                dependsOn,
                changeSetOperations
        );
        jpaSpecChangeSetMetadata.computeIfAbsent("", s -> new ArrayList<>())
                .add(changeSetMetadata);
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

    private Set<String> collectDependsOn(ChangeSet changeSet, Set<VariableElement> fields) {
        Stream<String> annotationDependsOn = Arrays.stream(changeSet.dependsOn());
        Stream<String> foreignKeyDependsOn = fields.stream()
                .filter(fieldElement -> fieldElement.getAnnotation(ForeignKey.class) != null)
                .map(fieldElement -> fieldElement.getAnnotation(ForeignKey.class))
                .map(ForeignKey::referenceTable);
        return Stream.concat(annotationDependsOn, foreignKeyDependsOn)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void collectColumnsChanges(Set<VariableElement> fieldsElements,
                                       String tableName,
                                       ChangeSet changeSet,
                                       Map<String, List<ChangeSetOperation>> changeSetAtBeginningOperations,
                                       Map<String, List<ChangeSetOperation>> changeSetCreateTableOperations,
                                       Map<String, List<ChangeSetOperation>> changeSetAtEndOperations) {
        Stream.Builder<AddColumnOp> addColumnOpsBuilder = Stream.builder();
        fieldsElements.forEach(fieldElement -> collectColumnsChanges(
                fieldElement,
                tableName,
                changeSet,
                addColumnOpsBuilder,
                changeSetAtBeginningOperations,
                changeSetAtEndOperations
        ));
        CreateTableOp createTableOp = addColumnOpsBuilder.build()
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedList::new),
                        f -> new CreateTableOp(tableName, f)
                ));
        LinkedList<ChangeSetOperation> tableOperations = new LinkedList<>();
        tableOperations.add(createTableOp);
        changeSetCreateTableOperations.put(changeSet.firstVersion(), tableOperations);
    }

    private void collectColumnsChanges(VariableElement fieldElement,
                                       String tableName,
                                       ChangeSet changeSet,
                                       Stream.Builder<AddColumnOp> addColumnOpsBuilder,
                                       Map<String, List<ChangeSetOperation>> changeSetAtBeginningOperations,
                                       Map<String, List<ChangeSetOperation>> changeSetAtEndOperations) {
        if (tableMetadataUtil.isEmbeddable(fieldElement)) {
            if (processingEnv.getTypeUtils().asElement(fieldElement.asType()) instanceof TypeElement fieldTypeElement) {
                collectEmbeddableColumnOperations(
                        fieldElement,
                        fieldTypeElement,
                        tableName,
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
                                                   ChangeSet changeSet,
                                                   Stream.Builder<AddColumnOp> addColumnOpsBuilder,
                                                   Map<String, List<ChangeSetOperation>> changeSetAtBeginningOperations,
                                                   Map<String, List<ChangeSetOperation>> changeSetAtEndOperations) {
        Set<VariableElement> embeddableFieldElements = getDeclaredFields(fieldTypeElement);
        if (tableMetadataUtil.isEmbeddableIdentifier(embeddableFieldElement)) {
            String fkName = "fk_" + tableName;
            AddUniqueConstraintOp addUniqueConstraintOp = embeddableFieldElements.stream()
                    .map(VariableElement::getSimpleName)
                    .map(Name::toString)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toCollection(LinkedHashSet::new),
                            f -> new AddUniqueConstraintOp(tableName, f, fkName)
                    ));
            changeSetAtEndOperations.computeIfAbsent(changeSet.firstVersion(), k -> new LinkedList<>())
                    .add(addUniqueConstraintOp);
        }
        embeddableFieldElements.forEach(fieldElement -> collectColumnOperations(
                fieldElement,
                tableName,
                changeSet,
                addColumnOpsBuilder,
                changeSetAtBeginningOperations,
                changeSetAtEndOperations
        ));
    }

    private void collectColumnOperations(
            VariableElement fieldElement,
            String tableName,
            ChangeSet changeSet,
            Stream.Builder<AddColumnOp> addColumnOpsBuilder,
            Map<String, List<ChangeSetOperation>> changeSetAtBeginningOperations,
            Map<String, List<ChangeSetOperation>> changeSetAtEndOperations) {
        String columnName = tableMetadataUtil.resolveColumnName(fieldElement);
        collectColumnOperations(
                fieldElement,
                changeSet,
                tableName,
                columnName,
                addColumnOpsBuilder,
                changeSetAtBeginningOperations,
                changeSetAtEndOperations
        );
    }

    private void collectColumnOperations(VariableElement fieldElement,
                                         ChangeSet changeSet,
                                         String tableName,
                                         String columnName,
                                         Stream.Builder<AddColumnOp> addColumnOpsBuilder,
                                         Map<String, List<ChangeSetOperation>> changeSetAtBeginningOperations,
                                         Map<String, List<ChangeSetOperation>> changeSetAtEndOperations) {
        AddColumnOp addColumnOp = resolveAddColumnOperations(
                fieldElement,
                tableName,
                columnName,
                fieldElement.getAnnotation(Column.class)
        );
        addColumnOpsBuilder.add(addColumnOp);
        if (tableMetadataUtil.isIdentifier(fieldElement) &&
                fieldElement.getAnnotation(SequenceGenerator.class) != null) {
            SequenceGenerator sequenceGenerator = fieldElement.getAnnotation(SequenceGenerator.class);
            CreateSequence createSequence = new CreateSequence(
                    sequenceGenerator.name(),
                    sequenceGenerator.initialValue(),
                    sequenceGenerator.allocationSize()
            );
            changeSetAtBeginningOperations.computeIfAbsent(changeSet.firstVersion(), k -> new LinkedList<>())
                    .add(createSequence);
        }
        List<ChangeSetOperation> atTheEndOperations = changeSetAtEndOperations
                .computeIfAbsent(changeSet.firstVersion(), k -> new LinkedList<>());
        addColumnOp.constraints().forEach(constraintMetadata -> {
            if (constraintMetadata.type() == UNIQUE) {
                atTheEndOperations.add(new AddUniqueConstraintOp(
                        tableName,
                        Collections.singleton(columnName),
                        constraintMetadata.name()
                ));
            }
            if (constraintMetadata.type() == FOREIGN_KEY && constraintMetadata.parameters().size() == 2) {
                atTheEndOperations.add(new AddForeignKeyConstraint(
                        tableName,
                        columnName,
                        constraintMetadata.name(),
                        constraintMetadata.parameters().get(0),
                        constraintMetadata.parameters().get(1)
                ));
            }
        });
        Optional.ofNullable(fieldElement.getAnnotation(ColumnChanges.class))
                .ifPresent(changes -> collectColumnChangesOperations(
                        changes,
                        changeSet,
                        tableName,
                        columnName,
                        changeSetAtBeginningOperations,
                        changeSetAtEndOperations
                ));
    }

    private AddColumnOp resolveAddColumnOperations(VariableElement fieldElement,
                                                   String tableName,
                                                   String columnName,
                                                   Column column) {
        AddColumnOp.AddColumnOpBuilder addColumnOp = AddColumnOp.builder()
                .column(columnName)
                .type(tableMetadataUtil.resolveType(fieldElement, column));
        if (tableMetadataUtil.isIdentifier(fieldElement)) {
            addColumnOp.constraint(ConstraintMetadata.builder()
                    .name("pk_" + tableName)
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
            if (!column.nullable()) {
                addColumnOp.constraint(ConstraintMetadata.builder()
                        .name("nonnull_" + tableName + '_' + columnName)
                        .type(NON_NULL)
                        .build());
            }
        }
        if (fieldElement.getAnnotation(ForeignKey.class) != null) {
            ForeignKey foreignKey = fieldElement.getAnnotation(ForeignKey.class);
            List<String> parameters = Stream.of(foreignKey.referenceTable(), foreignKey.referenceColumn())
                    .collect(Collectors.toCollection(ArrayList::new));
            addColumnOp.constraint(ConstraintMetadata.builder()
                    .name(foreignKey.name())
                    .parameters(parameters)
                    .type(FOREIGN_KEY)
                    .build());
        }
        return addColumnOp.build();
    }

    private void collectColumnChangesOperations(ColumnChanges columnChanges,
                                                ChangeSet changeSet,
                                                String tableName,
                                                String columnName,
                                                Map<String, List<ChangeSetOperation>> changeSetAtBeginningOperations,
                                                Map<String, List<ChangeSetOperation>> changeSetAtEndOperations) {
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
                                              Map<String, List<ChangeSetOperation>> changeSetAtBeginningOperations) {
        String version = columnChange.version().isEmpty() ? changeSet.firstVersion() : columnChange.version();
        List<ChangeSetOperation> atBeginningOperations =
                changeSetAtBeginningOperations.computeIfAbsent(version, k -> new LinkedList<>());
        atBeginningOperations.addAll(buildOperations(columnChange.operation(), tableName, columnName, columnChange));
    }

    private void collectAtEndOperations(ColumnChange columnChange,
                                        ChangeSet changeSet,
                                        String tableName,
                                        String columnName,
                                        Map<String, List<ChangeSetOperation>> changeSetAtEndOperations) {
        String version = columnChange.version().isEmpty() ? changeSet.firstVersion() : columnChange.version();
        List<ChangeSetOperation> atEndOperations =
                changeSetAtEndOperations.computeIfAbsent(version, k -> new LinkedList<>());
        atEndOperations.addAll(buildOperations(columnChange.operation(), tableName, columnName, columnChange));
    }

    private List<ChangeSetOperation> buildOperations(Operation operation,
                                                     String tableName,
                                                     String columnName,
                                                     ColumnChange columnChange) {
        if (operation == Operation.ADD_NOT_NULL_CONSTRAINT) {
            return Collections.singletonList(new AddNotNullConstraintOp(tableName, columnName));
        } else if (operation == Operation.DROP_DEFAULT_VALUE) {
            return Collections.singletonList(new DropDefaultValueOp(tableName, columnName));
        } else if (operation == Operation.DEFAULT_VALUE_CHANGE) {
            List<ChangeSetOperation> operations = new LinkedList<>();
            operations.add(new DropDefaultValueOp(tableName, columnName));
            operations.add(new AddDefaultValueOp(tableName, columnName, columnChange.parameters()));
            return operations;
        } else if (operation == Operation.DROP_NULL_CONSTRAINT) {
            return Collections.singletonList(new DropNotNullConstraintOp(tableName, columnName));
        } else if (operation == Operation.MODIFY_DATA_TYPE) {
            return Collections.singletonList(new ModifyDataTypeOp(tableName, columnName, columnChange.parameters()));
        } else {
            return Collections.emptyList();
        }
    }

    private ChangeSetOrderer buildOrderer(Map<String, ChangeSet> tableChangeSets,
                                          List<ChangeSetMetadata> changeSetMetadata) {
        return tableChangeSets.entrySet()
                .stream()
                .map(tableChangeSet ->
                        entry(tableChangeSet.getKey(), Arrays.asList(tableChangeSet.getValue().versionChronology())))
                .collect(Collectors.collectingAndThen(
                        Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue),
                        versions -> new ChangeSetOrderer(changeSetMetadata, versions)
                ));
    }

    private Map<TypeElement, ChangeSet> collectJpaEntities(Map<String, List<TypeElement>> jpaSpecEntities) {
        return jpaSpecEntities.values()
                .stream()
                .flatMap(List::stream)
                .filter(tableMetadataUtil::isJpaEntity)
                .filter(entity -> entity.getAnnotation(ChangeSet.class) != null)
                .map(element -> entry(element, element.getAnnotation(ChangeSet.class)))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Set<VariableElement> getDeclaredFields(TypeElement typeElement) {
        return typeElement.getEnclosedElements()
                .stream()
                .filter(element -> element.getKind() == FIELD)
                .filter(VariableElement.class::isInstance)
                .map(VariableElement.class::cast)
                .collect(Collectors.toCollection(LinkedHashSet::new));
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

    private String generateChangeLogName(ChangeSet changeSet, String version, String tableName) {
        String fileName = changeSet.file();
        fileName = fileName.replace("{version}", version);
        fileName = fileName.replace("{table}", tableName);
        return fileName;
    }

}
