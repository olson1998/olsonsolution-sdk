package com.olsonsolution.common.spring.application.annotation.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import com.olsonsolution.common.spring.application.annotation.migration.*;
import jakarta.persistence.Column;
import jakarta.persistence.SequenceGenerator;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.w3c.dom.Document;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
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

import static com.olsonsolution.common.spring.application.annotation.processor.ConstraintMetadata.Type.*;
import static java.util.Map.entry;
import static javax.lang.model.element.ElementKind.FIELD;
import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static javax.xml.transform.OutputKeys.*;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({
        "com.olsonsolution.common.spring.application.annotation.migration.ChangeSet"
})
public class ChangeSetAnnotationProcessor extends AbstractProcessor {

    private TableMetadataUtil tableMetadataUtil;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.objectMapper = new ObjectMapper();
        this.tableMetadataUtil = new TableMetadataUtil(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            List<ChangeSetMetadata> changeSetMetadata = collectChangeSetMetadata(roundEnv);
            Map<ChangeSetMetadata, Document> changeSetChangeLogs =
                    ChangeLogGenerator.generateChangeLogs(changeSetMetadata);
            Map.Entry<String, Document> masterChangeLog =
                    ChangeLogGenerator.generateMasterChangeLog(changeSetChangeLogs);
            for (Map.Entry<ChangeSetMetadata, Document> changeSetChangeLog : changeSetChangeLogs.entrySet()) {
                ChangeSetMetadata metadata = changeSetChangeLog.getKey();
                String changeLogLocation = "/db/changelog/" + metadata.changelogName();
                createChangeLogXml(changeLogLocation, changeSetChangeLog.getValue());
            }
            if (!changeSetChangeLogs.isEmpty()) {
                createChangeLogXml(masterChangeLog.getKey(), masterChangeLog.getValue());
            }
        } catch (Exception e) {
            String ThrowableMsg = ExceptionUtils.getStackTrace(e);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage() + "\n" + ThrowableMsg);
        }
        return true;
    }

    private List<ChangeSetMetadata> collectChangeSetMetadata(RoundEnvironment roundEnv) {
        Map<TypeElement, ChangeSet> changeSetEntities = collectJpaEntities(roundEnv);
        Stream.Builder<ChangeSetMetadata> changeSetMetadata = Stream.builder();
        Stream.Builder<Map.Entry<String, ChangeSet>> tableChangeSets = Stream.builder();
        changeSetEntities.forEach((typeElement, changeSet) -> collectChangesetMetadata(
                typeElement,
                changeSet,
                changeSetMetadata,
                tableChangeSets
        ));
        List<ChangeSetMetadata> unorderedChangeSetMetadata = changeSetMetadata.build()
                .toList();
        ChangeSetOrderer orderer = buildOrderer(tableChangeSets, unorderedChangeSetMetadata);
        return unorderedChangeSetMetadata.stream()
                .sorted(orderer)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private void collectChangesetMetadata(TypeElement typeElement,
                                          ChangeSet changeSet,
                                          Stream.Builder<ChangeSetMetadata> changeSetMetadata,
                                          Stream.Builder<Map.Entry<String, ChangeSet>> tableChangeSets) {
        String tableName = tableMetadataUtil.resolveTableName(typeElement);
        tableChangeSets.add(entry(tableName, changeSet));
        collectChangeSetMetadata(typeElement, tableName, changeSet, changeSetMetadata);
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
                                          String tableName,
                                          ChangeSet changeSet,
                                          Stream.Builder<ChangeSetMetadata> changeSetMetadata) {
        Map<String, List<ChangeSetOperation>> changeSetAtBeginningOperations = new HashMap<>();
        Map<String, List<ChangeSetOperation>> changeSetCreateTableOperations = new HashMap<>();
        Map<String, List<ChangeSetOperation>> changeSetAtEndOperations = new HashMap<>();
        Map<String, List<ChangeSetOperation>> changeSetOperations = new LinkedHashMap<>();
        Set<VariableElement> fields = getDeclaredFields(typeElement);
        collectColumnsChanges(
                fields,
                tableName,
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
        changeSetOperations.forEach((version, ops) ->
                collectChangeSetMetadata(version, ops, changeSet, tableName, typeElement, fields, changeSetMetadata));
    }

    private void collectChangeSetMetadata(String version,
                                          List<ChangeSetOperation> changeSetOperations,
                                          ChangeSet changeSet,
                                          String table,
                                          TypeElement typeElement,
                                          Set<VariableElement> fields,
                                          Stream.Builder<ChangeSetMetadata> changeSetMetadata) {
        String changeLogName = generateChangeLogName(changeSet, version, table);
        Set<String> dependsOn = collectDependsOn(changeSet, fields);
        changeSetMetadata.add(new ChangeSetMetadata(
                typeElement,
                table,
                version,
                changeSet.path(),
                changeLogName,
                dependsOn,
                changeSetOperations
        ));
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

    private ChangeSetOrderer buildOrderer(Stream.Builder<Map.Entry<String, ChangeSet>> tableChangeSets,
                                          List<ChangeSetMetadata> changeSetMetadata) {
        return tableChangeSets.build()
                .map(tableChangeSet ->
                        entry(tableChangeSet.getKey(), Arrays.asList(tableChangeSet.getValue().versionChronology())))
                .collect(Collectors.collectingAndThen(
                        Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue),
                        versions -> new ChangeSetOrderer(changeSetMetadata, versions)
                ));
    }

    private Map<TypeElement, ChangeSet> collectJpaEntities(RoundEnvironment roundEnv) {
        return roundEnv.getElementsAnnotatedWith(ChangeSet.class)
                .stream()
                .filter(tableMetadataUtil::isJpaEntity)
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
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
