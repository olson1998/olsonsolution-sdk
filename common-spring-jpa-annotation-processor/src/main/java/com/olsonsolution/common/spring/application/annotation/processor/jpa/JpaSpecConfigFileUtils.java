package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.olsonsolution.common.reflection.domain.port.repository.annotion.processor.MessagePrinter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.annotation.processing.Filer;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.olsonsolution.common.spring.application.annotation.processor.jpa.ChangeLogFactory.VALUE_XMLNS;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static javax.tools.StandardLocation.CLASS_PATH;
import static javax.xml.transform.OutputKeys.*;

@RequiredArgsConstructor
class JpaSpecConfigFileUtils {

    private static final String JPA_SPEC_JPA_CONFIGURER_TEMPLATE_FILE =
            "jpa/templates/_JpaSpecJpaConfigurer.template";

    private static final String MASTER_CHANGE_LOG_PATH = "db/changelog/db.changelog-master.xml";

    private static final String JPA_SPEC_PROCEDURES_YAML = "db/procedures/procedures.yaml";

    private static final String JPA_SPEC_CHANGE_LOG_PATH = "db/changelog/%s/%s.xml";

    private final Filer filer;

    private final MessagePrinter messagePrinter;

    private final ObjectMapper yamlMapper = yamlMapper();

    private final Set<String> generatedFiles = new HashSet<>();

    void createJpaSpecProceduresYaml(JpaSpecExecPlan jpaSpecExecPlan) {
        String proceduresYaml;
        try {
            proceduresYaml = yamlMapper.writeValueAsString(jpaSpecExecPlan);
        } catch (JsonProcessingException e) {
            messagePrinter.print(
                    Diagnostic.Kind.ERROR, JpaSpecConfigFileUtils.class,
                    "Jpa Spec configuration YAML was not serialized, reason:", e
            );
            return;
        }
        messagePrinter.print(Diagnostic.Kind.NOTE, JpaSpecConfigFileUtils.class, "\n" + proceduresYaml);
        try {
            createResource(JPA_SPEC_PROCEDURES_YAML, proceduresYaml);
            generatedFiles.add(JPA_SPEC_PROCEDURES_YAML);
        } catch (IOException e) {
            messagePrinter.print(
                    Diagnostic.Kind.ERROR, JpaSpecConfigFileUtils.class,
                    "Jpa Spec configuration %s was not created, reason:".formatted(JPA_SPEC_PROCEDURES_YAML), e
            );
        }
    }

    void createChangeLogs(Map<String, List<Document>> jpaSpecsChangeLogs) {
        for (Map.Entry<String, List<Document>> jpaSpecChangeLogs : jpaSpecsChangeLogs.entrySet()) {
            createChangeLogs(jpaSpecChangeLogs);
        }
    }

    void createMasterChangeLog(Document masterChangeLog) {
        createChangeLog(MASTER_CHANGE_LOG_PATH, masterChangeLog);
        generatedFiles.add(MASTER_CHANGE_LOG_PATH);
    }

    void createJpaConfigurationClasses(JpaSpecExecPlan execPlan) {
        for (JpaSpecProcedure procedure : execPlan.procedures()) {
            generateSpringConfigClass(procedure);
        }
    }

    private static ObjectMapper yamlMapper() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        SimpleModule module = new SimpleModule();
        module.addSerializer(TypeElement.class, new TypeElementStdSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }

    private void generateSpringConfigClass(JpaSpecProcedure procedure) {
        String jpaSpec = procedure.metadata().jpaSpec();
        String basePackage = procedure.metadata().jpaSpecConfigPackage();
        String className = basePackage + '.' + jpaSpec + "JpaSpecConfigurer";
        messagePrinter.print(Diagnostic.Kind.NOTE, JpaSpecConfigFileUtils.class, "Generating" + className);
        messagePrinter.print(
                Diagnostic.Kind.NOTE, JpaSpecConfigFileUtils.class,
                "Jpa Spec: %s Base package: %s".formatted(jpaSpec, basePackage)
        );
        Instant timestamp = Instant.now();
        try {
            String generatedClass = readClasspathFile(JPA_SPEC_JPA_CONFIGURER_TEMPLATE_FILE);
            generatedClass = generatedClass.replace("${BASE_PACKAGE}", basePackage);
            generatedClass = generatedClass.replace(
                    "${ANNOTATION_PROCESSOR}",
                    JpaSpecAnnotationProcessor.class.getCanonicalName()
            );
            String compiler = "";
            String entityBasePackages = collectToArray(procedure.metadata().entitiesPackages());
            String jpaReposBasePackages = collectToArray(procedure.metadata().jpaRepositoriesPackages());
            generatedClass = generatedClass.replace("${COMPILER}", compiler);
            generatedClass = generatedClass.replace("${JPA_SPEC}", jpaSpec);
            generatedClass = generatedClass.replace("${TIMESTAMP}", ISO_INSTANT.format(timestamp));
            generatedClass = generatedClass.replace("${ENTITY_BASE_PACKAGES}", entityBasePackages);
            generatedClass = generatedClass.replace("${JPA_REPOS_BASE_PACKAGES}", jpaReposBasePackages);
            generateClass(className, generatedClass);
        } catch (IOException e) {
            messagePrinter.print(
                    Diagnostic.Kind.ERROR, JpaSpecConfigFileUtils.class,
                    "%s was not generated, reason:".formatted(className), e
            );
        }
    }

    private void generateClass(String generatedClassName, String generatedClass) throws IOException {
        JavaFileObject classFile = filer.createSourceFile(generatedClassName);
        try (Writer writer = classFile.openWriter()) {
            writer.write(generatedClass);
        }
        generatedFiles.add(generatedClassName);
    }

    private void createChangeLogs(Map.Entry<String, List<Document>> jpaSpecChangeLogs) {
        for (Document changeLog : jpaSpecChangeLogs.getValue()) {
            String location = calculateLocation(jpaSpecChangeLogs.getKey(), changeLog);
            messagePrinter.print(Diagnostic.Kind.NOTE, JpaSpecConfigFileUtils.class,
                    "Generating change log %s".formatted(location));
            createChangeLog(location, changeLog);
            generatedFiles.add(location);
        }
    }

    private void createChangeLog(String location, Document changeLog) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(INDENT, "yes");
            transformer.setOutputProperty(OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(ENCODING, "UTF-8");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(changeLog), new StreamResult(writer));
            createResource(location, writer.toString());
        } catch (IOException e) {
            messagePrinter.print(
                    Diagnostic.Kind.ERROR, JpaSpecConfigFileUtils.class,
                    "Change log %s has not been created, reason:".formatted(location), e
            );
        } catch (TransformerException e) {
            messagePrinter.print(
                    Diagnostic.Kind.ERROR, JpaSpecConfigFileUtils.class,
                    "Change log %s can not be transformed to xml resource, reason:".formatted(location), e
            );
        }
    }

    private void createResource(@NonNull String location, @NonNull String content) throws IOException {
        if (generatedFiles.contains(location)) {
            messagePrinter.print(
                    Diagnostic.Kind.NOTE, JpaSpecConfigFileUtils.class,
                    "File %s already created".formatted(location)
            );
            return;
        }
        FileObject resourceObject = filer.createResource(
                CLASS_OUTPUT,
                "",
                location
        );
        try (Writer writer = resourceObject.openWriter()) {
            writer.write(content);
        }
    }

    private String readClasspathFile(String path) throws IOException {
        FileObject fileObject = filer.getResource(CLASS_PATH, "", path);
        return String.valueOf(fileObject.getCharContent(false));
    }

    private String calculateLocation(String jpaSpec, Document changeLog) {
        NodeList databaseChangeLogs = changeLog.getElementsByTagNameNS(VALUE_XMLNS, "databaseChangeLog");

        if (databaseChangeLogs.getLength() == 1 && databaseChangeLogs.item(0)
                instanceof Element databaseChangeLogsElement) {

        } else {
            throw new IllegalArgumentException("Change log must have only one databaseChangeLog");
        }
        NodeList changeSets = databaseChangeLogsElement.getElementsByTagNameNS(VALUE_XMLNS, "changeSet");
        if (changeSets.getLength() == 1 && changeSets.item(0) instanceof Element changeSetElement) {

        } else {
            throw new IllegalArgumentException("Change log must have only one databaseChangeLog");
        }
        String id = changeSetElement.getAttribute("id");
        return JPA_SPEC_CHANGE_LOG_PATH.formatted(
                jpaSpec,
                id
        );
    }

    private String collectToArray(Collection<String> basePackages) {
        return basePackages.stream()
                .map(basePackage -> "\"" + basePackage + "\"")
                .collect(Collectors.collectingAndThen(
                        Collectors.joining(","),
                        values -> "{" + values + "}"
                ));
    }

}
