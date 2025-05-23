package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.google.auto.service.AutoService;
import com.olsonsolution.common.reflection.domain.port.repository.annotion.processor.MessagePrinter;
import com.olsonsolution.common.reflection.domain.service.annotion.processor.MessagePrintingService;
import org.apache.commons.collections4.CollectionUtils;
import org.w3c.dom.Document;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({
        "com.olsonsolution.common.spring.application.annotation.jpa.JpaSpec",
        "com.olsonsolution.common.spring.application.annotation.jpa.EnableJpaSpec",
        "com.olsonsolution.common.spring.application.annotation.migration.ChangeSet"
})
public class JpaSpecAnnotationProcessor extends AbstractProcessor {

    private MessagePrinter messagePrinter;

    private ChangeLogFactory changeLogFactory;

    private JpaSpecProcedureFactory jpaSpecProcedureFactory;

    private JpaSpecConfigUtil jpaSpecConfigUtil;

    private JpaSpecConfigFileUtils jpaSpecConfigFileUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messagePrinter = new MessagePrintingService(processingEnv.getMessager());
        TableMetadataUtil tableMetadataUtil = new TableMetadataUtil(messagePrinter, processingEnv);
        ChangeLogOrderer changeLogOrderer = new ChangeLogOrderer(messagePrinter);
        this.changeLogFactory = new ChangeLogFactory(messagePrinter);
        this.jpaSpecProcedureFactory = new JpaSpecProcedureFactory(changeLogOrderer, processingEnv, tableMetadataUtil);
        this.jpaSpecConfigUtil = new JpaSpecConfigUtil(
                processingEnv.getElementUtils(),
                messagePrinter,
                tableMetadataUtil,
                processingEnv
        );
        this.jpaSpecConfigFileUtils = new JpaSpecConfigFileUtils(processingEnv.getFiler(), messagePrinter);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            List<JpaSpecMetadata> jpaSpecMetadata = jpaSpecConfigUtil.mirrorJpaSpecs(roundEnv);
            if (CollectionUtils.isEmpty(jpaSpecMetadata)) {
                return true;
            }
            for (JpaSpecMetadata spec : jpaSpecMetadata) {
                messagePrinter.print(
                        Diagnostic.Kind.NOTE,
                        JpaSpecAnnotationProcessor.class,
                        spec.toString()
                );
            }
            JpaSpecExecPlan jpaSpecExecPlan = jpaSpecProcedureFactory.fabricate(jpaSpecMetadata);
            jpaSpecConfigFileUtils.createJpaSpecProceduresYaml(jpaSpecExecPlan);
            jpaSpecConfigFileUtils.createJpaConfigurationClasses(jpaSpecExecPlan);
            Map<String, List<Document>> jpaSpecChangeLogs = changeLogFactory.createChangeLogs(jpaSpecExecPlan);
            Document masterChangeLog = changeLogFactory.generateMasterChangeLog(jpaSpecExecPlan);
            jpaSpecConfigFileUtils.createChangeLogs(jpaSpecChangeLogs);
            jpaSpecConfigFileUtils.createMasterChangeLog(masterChangeLog);
        } catch (Exception e) {
            messagePrinter.print(
                    Diagnostic.Kind.ERROR,
                    JpaSpecAnnotationProcessor.class,
                    "Error:",
                    e
            );
        }
        return true;
    }

}
