package com.olsonsolution.common.spring.application.annotation.processor.migration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.olsonsolution.common.spring.application.annotation.processor.migration.ConstraintMetadata.Type.NON_NULL;
import static com.olsonsolution.common.spring.application.annotation.processor.migration.ConstraintMetadata.Type.PRIMARY_KEY;
import static java.util.Map.entry;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ChangeLogGenerator {

    private static final String VALUE_XMLNS = "http://www.liquibase.org/xml/ns/dbchangelog";
    private static final String VALUE_XMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String VALUE_SCHEMA_LOCATION = "http://www.liquibase.org/xml/ns/dbchangelog " +
            "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.4.xsd";

    static Map<ChangeSetMetadata, Document> generateChangeLogs(
            List<ChangeSetMetadata> changeSetMetadata) throws ParserConfigurationException {
        Stream.Builder<Map.Entry<ChangeSetMetadata, Document>> changeLogsCollector = Stream.builder();
        for (ChangeSetMetadata changeSet : changeSetMetadata) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            String version = changeSet.version();
            generateChangeLog(changeSet.table(), version, changeSet.operations(), document);
            changeLogsCollector.add(entry(changeSet, document));
        }
        return changeLogsCollector.build()
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedList::new),
                        changeSetChangeLogs -> {
                            Map<ChangeSetMetadata, Document> changeLogs =
                                    new LinkedHashMap<>(changeSetChangeLogs.size());
                            for (Map.Entry<ChangeSetMetadata, Document> changeSetChangeLog : changeSetChangeLogs) {
                                changeLogs.put(changeSetChangeLog.getKey(), changeSetChangeLog.getValue());
                            }
                            return changeLogs;
                        }
                ));
    }

    static Document generateMasterChangeLog(
            Map<ChangeSetMetadata, Document> changeSetChangeLogs) throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element root = document.createElementNS(VALUE_XMLNS, "databaseChangeLog");
        root.setAttributeNS(XMLNS_ATTRIBUTE_NS_URI, "xmlns", VALUE_XMLNS);
        root.setAttributeNS(XMLNS_ATTRIBUTE_NS_URI, "xmlns:xsi", VALUE_XMLNS_XSI);
        root.setAttributeNS(W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi:schemaLocation", VALUE_SCHEMA_LOCATION);
        for (ChangeSetMetadata changeSet : changeSetChangeLogs.keySet()) {
            Element element = document.createElementNS(VALUE_XMLNS, "include");
            element.setAttribute("file", changeSet.changelogName());
            element.setAttribute("relativeToChangelogFile", "true");
            root.appendChild(element);
        }
        document.appendChild(root);
        return document;
    }

    private static void generateChangeLog(String table,
                                          String version,
                                          List<ChangeSetOperation> operations,
                                          Document document) {
        Element root = document.createElementNS(VALUE_XMLNS, "databaseChangeLog");
        root.setAttributeNS(XMLNS_ATTRIBUTE_NS_URI, "xmlns", VALUE_XMLNS);
        root.setAttributeNS(XMLNS_ATTRIBUTE_NS_URI, "xmlns:xsi", VALUE_XMLNS_XSI);
        root.setAttributeNS(W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi:schemaLocation", VALUE_SCHEMA_LOCATION);
        document.appendChild(root);
        Element changeSet = document.createElementNS(VALUE_XMLNS, "changeSet");
        changeSet.setAttribute("id", "changelog-" + table + '-' + version + "-changeset");
        changeSet.setAttribute("author", "${author}");
        for (ChangeSetOperation changeSetOperation : operations) {
            if (changeSetOperation instanceof CreateTableOp createTableOp) {
                generateCreateTable(createTableOp, changeSet, document);
            }
            if (changeSetOperation instanceof CreateSequence createSequence) {
                generateSequence(createSequence, changeSet, document);
            }
            if (changeSetOperation instanceof AddUniqueConstraintOp addUniqueConstraintOp) {
                generateUniqueConstraint(addUniqueConstraintOp, changeSet, document);
            }
            if (changeSetOperation instanceof AddNotNullConstraintOp addNotNullConstraintOp) {
                generateAddNotNullConstraint(addNotNullConstraintOp, changeSet, document);
            }
            if (changeSetOperation instanceof AddForeignKeyConstraint addForeignKeyConstraint) {
                generateForeignKeyConstraint(addForeignKeyConstraint, changeSet, document);
            }
            if (changeSetOperation instanceof DropNotNullConstraintOp dropNotNullConstraintOp) {
                generateDropNotNullConstraint(dropNotNullConstraintOp, changeSet, document);
            }
            if (changeSetOperation instanceof ModifyDataTypeOp modifyDataTypeOp) {
                generateModifyDataType(modifyDataTypeOp, changeSet, document);
            }
            if (changeSetOperation instanceof DropForeignKeyOp dropForeignKeyOp) {
                generateDropForeignKey(dropForeignKeyOp, changeSet, document);
            }
        }
        root.appendChild(changeSet);
    }

    private static void generateCreateTable(CreateTableOp createTableOp,
                                            Element changeSet,
                                            Document document) {
        Element createTable = document.createElement("createTable");
        createTable.setAttribute("schemaName", "${schema}");
        createTable.setAttribute("tableName", createTableOp.table());
        createTableOp.addColumns().forEach(column -> generateColumn(column, createTable, document));
        changeSet.appendChild(createTable);
    }

    private static void generateSequence(CreateSequence createSequence, Element changeSet, Document document) {
        Element sequence = document.createElement("createSequence");
        sequence.setAttribute("schemaName", "${schema}");
        sequence.setAttribute("sequenceName", createSequence.table());
        sequence.setAttribute("startValue", String.valueOf(createSequence.startValue()));
        sequence.setAttribute("incrementBy", String.valueOf(createSequence.incrementBy()));
        changeSet.appendChild(sequence);
    }

    private static void generateUniqueConstraint(AddUniqueConstraintOp addUniqueConstraintOp,
                                                 Element changeSet,
                                                 Document document) {
        Element unique = document.createElement("addUniqueConstraint");
        unique.setAttribute("schemaName", "${schema}");
        unique.setAttribute("tableName", addUniqueConstraintOp.table());
        unique.setAttribute("columnNames", addUniqueConstraintOp.columnNames());
        unique.setAttribute("constraintName", addUniqueConstraintOp.name());
        changeSet.appendChild(unique);
    }

    private static void generateAddNotNullConstraint(AddNotNullConstraintOp addNotNullConstraintOp,
                                                     Element changeSet,
                                                     Document document) {
        Element unique = document.createElement("addUniqueConstraint");
        unique.setAttribute("schemaName", "${schema}");
        unique.setAttribute("tableName", addNotNullConstraintOp.table());
        unique.setAttribute("columnNames", addNotNullConstraintOp.column());
        changeSet.appendChild(unique);
    }

    private static void generateForeignKeyConstraint(AddForeignKeyConstraint addForeignKeyConstraint,
                                                     Element changeSet,
                                                     Document document) {
        Element foreignKey = document.createElement("addForeignKeyConstraint");
        foreignKey.setAttribute("baseTableSchemaName", "${schema}");
        foreignKey.setAttribute("baseTableName", addForeignKeyConstraint.table());
        foreignKey.setAttribute("baseColumnNames", addForeignKeyConstraint.column());
        foreignKey.setAttribute("referencedTableSchemaName", "${schema}");
        foreignKey.setAttribute("referencedTableName", addForeignKeyConstraint.referencedTable());
        foreignKey.setAttribute("referencedColumnNames", addForeignKeyConstraint.referencedColumn());
        foreignKey.setAttribute("constraintName", addForeignKeyConstraint.constraintName());
        changeSet.appendChild(foreignKey);
    }

    private static void generateDropNotNullConstraint(DropNotNullConstraintOp dropNotNullConstraintOp,
                                                      Element changeSet,
                                                      Document document) {
        Element dropNotNullConstraint = document.createElement("dropNotNullConstraint");
        dropNotNullConstraint.setAttribute("schemaName", "${schema}");
        dropNotNullConstraint.setAttribute("tableName", dropNotNullConstraintOp.table());
        dropNotNullConstraint.setAttribute("columnName", dropNotNullConstraintOp.column());
        changeSet.appendChild(dropNotNullConstraint);
    }

    private static void generateModifyDataType(ModifyDataTypeOp modifyDataTypeOp,
                                               Element changeSet,
                                               Document document) {
        Element modifyDataType = document.createElement("modifyDataType");
        modifyDataType.setAttribute("schemaName", "${schema}");
        modifyDataType.setAttribute("tableName", modifyDataTypeOp.table());
        modifyDataType.setAttribute("columnName", modifyDataTypeOp.column());
        modifyDataType.setAttribute("newDataType", modifyDataTypeOp.columnDataType());
        changeSet.appendChild(modifyDataType);
    }

    private static void generateDropForeignKey(DropForeignKeyOp dropForeignKeyOp,
                                               Element changeSet,
                                               Document document) {
        Element dropForeignKey = document.createElement("dropForeignKeyConstraint");
        dropForeignKey.setAttribute("baseTableName", dropForeignKeyOp.table());
        dropForeignKey.setAttribute("constraintName", dropForeignKeyOp.constraintName());
        changeSet.appendChild(dropForeignKey);
    }

    private static void generateColumn(AddColumnOp addColumnOp, Element createTable, Document document) {
        Element column = document.createElement("column");
        column.setAttribute("name", addColumnOp.column());
        column.setAttribute("type", addColumnOp.type());
        Element constraints = null;
        for (ConstraintMetadata constraint : addColumnOp.constraints()) {
            if (constraint.type() == PRIMARY_KEY || constraint.type() == NON_NULL) {
                if (constraints == null) {
                    constraints = document.createElement("constraints");
                }
                if (constraint.type() == PRIMARY_KEY) {
                    constraints.setAttribute("primaryKey", "true");
                }
                if (constraint.type() == NON_NULL) {
                    constraints.setAttribute("nullable", "false"); //
                }
            }
        }
        if (constraints != null) {
            column.appendChild(constraints);
        }
        createTable.appendChild(column);
    }

}
