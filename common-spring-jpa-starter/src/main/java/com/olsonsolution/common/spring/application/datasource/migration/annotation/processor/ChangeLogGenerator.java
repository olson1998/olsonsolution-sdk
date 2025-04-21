package com.olsonsolution.common.spring.application.datasource.migration.annotation.processor;

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

import static com.olsonsolution.common.spring.application.datasource.migration.annotation.processor.ConstraintMetadata.Type.NON_NULL;
import static com.olsonsolution.common.spring.application.datasource.migration.annotation.processor.ConstraintMetadata.Type.PRIMARY_KEY;
import static java.util.Map.entry;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ChangeLogGenerator {

    private static final String VALUE_XMLNS = "http://www.liquibase.org/xml/ns/dbchangelog";
    private static final String VALUE_XMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String VALUE_SCHEMA_LOCATION = "http://www.liquibase.org/xml/ns/dbchangelog " +
            "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.4.xsd";

    static Map<String, Document> generateChangeLogs(
            String table,
            Map<String, List<ChangeSetOperation>> changeSetOperations) throws ParserConfigurationException {
        Stream.Builder<Map.Entry<String, Document>> changeLogsCollector = Stream.builder();
        for (Map.Entry<String, List<ChangeSetOperation>> versionOps : changeSetOperations.entrySet()) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            generateChangeLog(table, versionOps.getKey(), versionOps.getValue(), document);
            changeLogsCollector.add(entry(versionOps.getKey(), document));
        }
        return changeLogsCollector.build()
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedList::new),
                        f -> {
                            Map<String, Document> changeLogs = new LinkedHashMap<>(f.size());
                            f.forEach(entry -> changeLogs.put(entry.getKey(), entry.getValue()));
                            return changeLogs;
                        }
                ));
    }

    private static void generateChangeLog(String table,
                                          String version,
                                          List<ChangeSetOperation> operations,
                                          Document document) {
        Element root = document.createElement("databaseChangeLog");
        root.setAttribute("xmlns", VALUE_XMLNS);
        root.setAttribute("xmlns:xsi", VALUE_XMLNS_XSI);
        root.setAttribute("xsi:schemaLocation", VALUE_SCHEMA_LOCATION);
        document.appendChild(root);
        Element changeSet = document.createElement("changeSet");
        changeSet.setAttribute("id", table + "_" + version + "_changeset");
        changeSet.setAttribute("author", "${author}");
        for (ChangeSetOperation changeSetOperation : operations) {
            if (changeSetOperation instanceof CreateTableOp createTableOp) {
                generateCreateTable(createTableOp, changeSet, document);
            }
            if (changeSetOperation instanceof CreateSequence createSequence) {
                generateSequence(createSequence, changeSet, document);
            }
            if (changeSetOperation instanceof AddUniqueConstraint addUniqueConstraint) {
                generateUniqueConstraint(addUniqueConstraint, changeSet, document);
            }
            if (changeSetOperation instanceof AddForeignKeyConstraint addForeignKeyConstraint) {
                generateForeignKeyConstraint(addForeignKeyConstraint, changeSet, document);
            }
            if (changeSetOperation instanceof DropNotNullConstraintOp dropNotNullConstraintOp) {
                generateDropNotNullConstraint(dropNotNullConstraintOp, changeSet, document);
            }
        }
        root.appendChild(changeSet);
    }

    private static void generateCreateTable(CreateTableOp createTableOp,
                                            Element changeSet,
                                            Document document) {
        Element createTable = document.createElement("createTable");
        createTable.setAttribute("tableName", createTableOp.table());
        createTableOp.addColumns().forEach(column -> generateColumn(column, createTable, document));
        changeSet.appendChild(createTable);
    }

    private static void generateSequence(CreateSequence createSequence, Element changeSet, Document document) {
        Element sequence = document.createElement("createSequence");
        sequence.setAttribute("sequenceName", createSequence.table());
        sequence.setAttribute("startValue", String.valueOf(createSequence.startValue()));
        sequence.setAttribute("incrementBy", String.valueOf(createSequence.incrementBy()));
        changeSet.appendChild(sequence);
    }

    private static void generateUniqueConstraint(AddUniqueConstraint addUniqueConstraint,
                                                 Element changeSet,
                                                 Document document) {
        Element unique = document.createElement("addUniqueConstraint");
        unique.setAttribute("tableName", addUniqueConstraint.table());
        unique.setAttribute("columnNames", addUniqueConstraint.column());
        unique.setAttribute("constraintName", addUniqueConstraint.name());
        changeSet.appendChild(unique);
    }

    private static void generateForeignKeyConstraint(AddForeignKeyConstraint addForeignKeyConstraint,
                                                     Element changeSet,
                                                     Document document) {
        Element foreignKey = document.createElement("addForeignKeyConstraint");
        foreignKey.setAttribute("tableName", addForeignKeyConstraint.table());
        foreignKey.setAttribute("columnNames", addForeignKeyConstraint.column());
        foreignKey.setAttribute("referencedTableName", addForeignKeyConstraint.referencedTable());
        foreignKey.setAttribute("referencedColumnNames", addForeignKeyConstraint.referencedColumn());
        foreignKey.setAttribute("constraintName", addForeignKeyConstraint.constraintName());
        changeSet.appendChild(foreignKey);
    }

    private static void generateDropNotNullConstraint(DropNotNullConstraintOp dropNotNullConstraintOp,
                                                      Element changeSet,
                                                      Document document) {
        Element dropNotNullConstraint = document.createElement("dropNotNullConstraint");
        dropNotNullConstraint.setAttribute("tableName", dropNotNullConstraintOp.table());
        dropNotNullConstraint.setAttribute("columnName", dropNotNullConstraintOp.column());
        changeSet.appendChild(dropNotNullConstraint);
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
