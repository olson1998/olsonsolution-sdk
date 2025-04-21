package com.olsonsolution.common.spring.application.datasource.migration.annotation.processor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ChangeLogGenerator {

    private static final String VALUE_XMLNS = "http://www.liquibase.org/xml/ns/dbchangelog";
    private static final String VALUE_XMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String VALUE_SCHEMA_LOCATION = "http://www.liquibase.org/xml/ns/dbchangelog " +
            "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.4.xsd";

    public static List<Document> generateChangeLogs(
            String table,
            Map<String, List<ChangeSetOperation>> changeSetOperations) throws ParserConfigurationException {
        Stream.Builder<Document> changeLogsCollector = Stream.builder();
        for (Map.Entry<String, List<ChangeSetOperation>> versionOps : changeSetOperations.entrySet()) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            generateChangeLog(table, versionOps.getKey(), versionOps.getValue(), document);
            changeLogsCollector.add(document);
        }
        return changeLogsCollector.build()
                .collect(Collectors.toCollection(LinkedList::new));
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
    }

    private void generateCreateTable(CreateT)

}
