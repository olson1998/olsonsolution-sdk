package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.olsonsolution.common.reflection.domain.port.repository.annotion.processor.MessagePrinter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.tools.Diagnostic;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

@RequiredArgsConstructor
class ChangeLogFactory {

    public static final String VALUE_XMLNS = "http://www.liquibase.org/xml/ns/dbchangelog";
    public static final String VALUE_XMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String VALUE_SCHEMA_LOCATION = "http://www.liquibase.org/xml/ns/dbchangelog " +
            "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.4.xsd";

    private final MessagePrinter messagePrinter;


    Map<String, List<Document>> createChangeLogs(JpaSpecExecPlan jpaSpecExecPlan) {
        Map<String, List<Document>> jpaSpecChangeLogs = new HashMap<>(jpaSpecExecPlan.jpaSpecNames().size());
        try {
            for (JpaSpecProcedure procedure : jpaSpecExecPlan.procedures()) {
                generateChangeLogs(procedure, jpaSpecChangeLogs);
                List<Document> changeLogs =
                        ListUtils.emptyIfNull(jpaSpecChangeLogs.get(procedure.metadata().jpaSpec()));
                jpaSpecChangeLogs.put(procedure.metadata().jpaSpec(), changeLogs);
            }
        } catch (ParserConfigurationException e) {
            messagePrinter.print(
                    Diagnostic.Kind.ERROR,
                    ChangeLogFactory.class,
                    "Failed to generate change logs, reason",
                    e
            );
        }
        return jpaSpecChangeLogs;
    }

    Document generateMasterChangeLog(JpaSpecExecPlan jpaSpecExecPlan) {
        try {
            return generateMasterChangeLogUnsafe(jpaSpecExecPlan);
        } catch (ParserConfigurationException e) {
            messagePrinter.print(
                    Diagnostic.Kind.ERROR,
                    ChangeLogFactory.class,
                    "Failed to generate change logs, reason",
                    e
            );
            return null;
        }
    }

    private void generateChangeLogs(JpaSpecProcedure procedure,
                                    Map<String, List<Document>> jpaSpecChangeLogs) throws ParserConfigurationException {
        String jpaSpec = procedure.metadata().jpaSpec();
        List<ChangeSetOp> changeSets = procedure.changeSets();
        for (ChangeSetOp changeSetOp : changeSets) {
            Document changeLog = generateChangeLog(changeSetOp);
            jpaSpecChangeLogs
                    .computeIfAbsent(jpaSpec, k -> new ArrayList<>(changeSets.size() + 1))
                    .add(changeLog);
        }
    }

    private Document generateChangeLog(ChangeSetOp changeSetOp)
            throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        generateChangeLog(changeSetOp, document);
        return document;
    }

    private Document generateMasterChangeLogUnsafe(JpaSpecExecPlan jpaSpecExecPlan)
            throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element root = document.createElementNS(VALUE_XMLNS, "databaseChangeLog");
        root.setAttributeNS(XMLNS_ATTRIBUTE_NS_URI, "xmlns", VALUE_XMLNS);
        root.setAttributeNS(XMLNS_ATTRIBUTE_NS_URI, "xmlns:xsi", VALUE_XMLNS_XSI);
        root.setAttributeNS(W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi:schemaLocation", VALUE_SCHEMA_LOCATION);
        for (String changeLogLocation : jpaSpecExecPlan.changeLogsOrder()) {
            Element element = document.createElementNS(VALUE_XMLNS, "include");
            element.setAttribute("file", changeLogLocation);
            element.setAttribute("relativeToChangelogFile", "true");
            root.appendChild(element);
        }
        document.appendChild(root);
        return document;
    }

    private void generateChangeLog(ChangeSetOp changeSetOp,
                                   Document document) {
        Element root = document.createElementNS(VALUE_XMLNS, "databaseChangeLog");
        root.setAttributeNS(XMLNS_ATTRIBUTE_NS_URI, "xmlns", VALUE_XMLNS);
        root.setAttributeNS(XMLNS_ATTRIBUTE_NS_URI, "xmlns:xsi", VALUE_XMLNS_XSI);
        root.setAttributeNS(W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi:schemaLocation", VALUE_SCHEMA_LOCATION);
        Element changeSet = document.createElementNS(VALUE_XMLNS, "changeSet");
        changeSet.setAttribute("id", changeSetOp.id());
        changeSet.setAttribute("author", "${author}");
        for (ChangeOp changesOp : changeSetOp.operations()) {
            generateChange(changesOp, document, changeSet);
        }
        document.appendChild(root);
        root.appendChild(changeSet);
    }

    private void generateChange(ChangeOp changesOp, Document document, Element parentElement) {
        Element changeElement = document.createElement(changesOp.operation());
        for (Map.Entry<String, String> attribute : MapUtils.emptyIfNull(changesOp.attributes()).entrySet()) {
            changeElement.setAttribute(attribute.getKey(), attribute.getValue());
        }
        for (ChangeOp childChangesOp : ListUtils.emptyIfNull(changesOp.childOperations())) {
            generateChange(childChangesOp, document, changeElement);
        }
        parentElement.appendChild(changeElement);
    }

}
