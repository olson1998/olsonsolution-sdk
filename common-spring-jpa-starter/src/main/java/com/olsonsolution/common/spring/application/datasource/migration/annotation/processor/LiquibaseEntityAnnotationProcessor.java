package com.olsonsolution.common.spring.application.datasource.migration.annotation.processor;

import com.google.auto.service.AutoService;
import com.olsonsolution.common.spring.application.datasource.migration.annotation.*;
import com.olsonsolution.common.spring.application.datasource.migration.annotation.ForeignKey;
import jakarta.persistence.*;
import org.w3c.dom.Document;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.olsonsolution.common.spring.application.datasource.migration.annotation.processor.ConstraintMetadata.Type.*;
import static java.util.Map.entry;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.FIELD;
import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static javax.xml.transform.OutputKeys.*;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({
        "com.olsonsolution.common.spring.application.datasource.migration.annotation.ChangeSet"
})
public class LiquibaseEntityAnnotationProcessor extends AbstractProcessor {

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
        String tableName = resolveTableName(typeElement);
        Map<String, List<ChangeSetOperation>> changeSetOperations =
                collectOperations(typeElement, tableName, changeSet);
        try {
            Messager messager = processingEnv.getMessager();
            messager.printMessage(Diagnostic.Kind.NOTE, "changeSet=" + changeSetOperations);
            Map<String, Document> changeLogXmlList =
                    ChangeLogGenerator.generateChangeLogs(tableName, changeSetOperations);
            messager.printMessage(Diagnostic.Kind.NOTE, "changeLogXml=" + changeLogXmlList);
            for (Map.Entry<String, Document> versionChangeLogXml : changeLogXmlList.entrySet()) {
                String version = versionChangeLogXml.getKey();
                Document changeLogXml = versionChangeLogXml.getValue();
                createChangeLogXml(changeSet, tableName, version, changeLogXml);
            }
        } catch (ParserConfigurationException| TransformerException | IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    private void createChangeLogXml(ChangeSet changeSet,
                                    String tableName,
                                    String version,
                                    Document changeLogXml) throws IOException, TransformerException {
        String fileName = changeSet.file();
        fileName = fileName.replace("{version}", version);
        fileName = fileName.replace("{table}", tableName);
        Filer filer = processingEnv.getFiler();
        FileObject changeLogFile = filer.createResource(
                CLASS_OUTPUT,
                "",
                changeSet.path() + fileName
        );
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(INDENT, "yes");
        transformer.setOutputProperty(OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(METHOD, "xml");
        transformer.setOutputProperty(ENCODING, "UTF-8");
        try (Writer writer = changeLogFile.openWriter()) {
            StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(changeLogXml), new StreamResult(stringWriter));
            writer.write(stringWriter.toString());
        }
    }

    private Map<String, List<ChangeSetOperation>> collectOperations(TypeElement typeElement,
                                                                    String tableName,
                                                                    ChangeSet changeSet) {
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
        if (isEmbeddable(fieldElement)) {

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

    private void collectColumnOperations(
            VariableElement fieldElement,
            String tableName,
            ChangeSet changeSet,
            Stream.Builder<AddColumnOp> addColumnOpsBuilder,
            Map<String, List<ChangeSetOperation>> changeSetAtBeginningOperations,
            Map<String, List<ChangeSetOperation>> changeSetAtEndOperations) {
        String columnName = resolveColumnName(fieldElement);
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
        if (isIdentifier(fieldElement) && fieldElement.getAnnotation(SequenceGenerator.class) != null) {
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
                atTheEndOperations.add(new AddUniqueConstraint(tableName, columnName, constraintMetadata.name()));
            }
            if (constraintMetadata.type() == FOREIGN_KEY) {
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
                .type(resolveType(fieldElement, column));
        if (isIdentifier(fieldElement)) {
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
            if (column.nullable()) {
                addColumnOp.constraint(ConstraintMetadata.builder()
                        .name("nonnull_" + tableName + '_' + columnName)
                        .type(NON_NULL)
                        .build());
            }
        }
        if (fieldElement.getAnnotation(ForeignKey.class) != null) {
            ForeignKey foreignKey = fieldElement.getAnnotation(ForeignKey.class);
            List<String> parameters = new ArrayList<>(2);
            parameters.set(0, foreignKey.referenceTable());
            parameters.set(1, foreignKey.referenceColumn());
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

    private String resolveType(VariableElement variableElement, Column column) {
        if (column != null && !column.columnDefinition().isEmpty()) {
            return column.columnDefinition();
        }
        return assumeType(variableElement, column);
    }

    private String assumeType(VariableElement variableElement, Column column) {
        TypeMirror variableTypeMirror = variableElement.asType();
        if (variableTypeMirror.getKind().isPrimitive()) {
            return assumePrimitiveType(variableTypeMirror);
        } else if (isAssignableFieldType(variableElement, Integer.class)) {
            return "INT";
        } else if (isAssignableFieldType(variableElement, Long.class) ||
                isAssignableFieldType(variableElement, BigInteger.class)) {
            return "BIGINT";
        } else if (isAssignableFieldType(variableElement, Short.class)) {
            return "SMALLINT";
        } else if (isAssignableFieldType(variableElement, Double.class) ||
                isAssignableFieldType(variableElement, BigDecimal.class)) {
            return "double";
        } else if (isAssignableFieldType(variableElement, Float.class)) {
            return "float";
        } else if (isAssignableFieldType(variableElement, Boolean.class)) {
            return "boolean";
        } else if (isAssignableFieldType(variableElement, Character.class)) {
            return "varchar(1)";
        } else if (isAssignableFieldType(variableElement, String.class) && column != null) {
            return "varchar(" + column.length() + ")";
        } else if (isAssignableFieldType(variableElement, String.class)) {
            return "varchar(255)";
        } else {
            return "";
        }
    }

    private String assumePrimitiveType(TypeMirror typeMirror) {
        switch (typeMirror.getKind()) {
            case INT -> {
                return "INT";
            }
            case LONG -> {
                return "BIGINT";
            }
            case SHORT -> {
                return "SMALLINT";
            }
            case DOUBLE -> {
                return "double";
            }
            case FLOAT -> {
                return "float";
            }
            case BOOLEAN -> {
                return "boolean";
            }
            case CHAR -> {
                return "varchar(1)";
            }
        }
        return "";
    }

    private TypeMirror getDeclaredType(Class<?> javaClass, Types typeUtils, Elements elementUtils) {
        TypeElement typeElement = elementUtils.getTypeElement(javaClass.getCanonicalName());
        return typeUtils.getDeclaredType(typeElement);
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

    private boolean isAssignableFieldType(VariableElement element, Class<?> javaClass) {
        Types typeUtils = processingEnv.getTypeUtils();
        Elements elementUtils = processingEnv.getElementUtils();
        TypeMirror typeMirror = getDeclaredType(javaClass, typeUtils, elementUtils);
        return typeUtils.isAssignable(element.asType(), typeMirror);
    }

}
