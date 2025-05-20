package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.google.auto.service.AutoService;
import com.olsonsolution.common.reflection.domain.port.repository.annotion.processor.MessagePrinter;
import com.olsonsolution.common.reflection.domain.service.annotion.processor.MessagePrintingService;
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
        TableMetadataUtil tableMetadataUtil = new TableMetadataUtil(processingEnv);
        this.messagePrinter = new MessagePrintingService(processingEnv.getMessager());
        this.changeLogFactory = new ChangeLogFactory(messagePrinter);
        this.jpaSpecProcedureFactory = new JpaSpecProcedureFactory(processingEnv, tableMetadataUtil);
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

//    private void processConfig(Map<String, Map<TypeElement, DeclaredType>> jpaSpecRepoConfig,
//                               List<TypeElement> enableJpaSpecElements) {
//        jpaSpecRepoConfig.forEach((jpaSpec, jpaRepos) -> generateSpringJpaConfig(
//                jpaSpec,
//                jpaRepos,
//                enableJpaSpecElements
//        ));
//    }
//
//    private void generateSpringJpaConfig(String jpaSpec,
//                                         Map<TypeElement, DeclaredType> jpaRepoElements,
//                                         List<TypeElement> enableJpaSpecElements) {
//        if (enableJpaSpecElements.isEmpty()) {
//            return;
//        }
//        String configPackage = processingEnv.getElementUtils()
//                .getPackageOf(enableJpaSpecElements.get(0))
//                .getQualifiedName().toString();
//        if (enableJpaSpecElements.size() > 1) {
//            processingEnv.getMessager().printMessage(
//                    Diagnostic.Kind.NOTE,
//                    "Multiple @EnableJpaSpec declared. Using: " + configPackage
//            );
//        }
//        generateSpringJpaConfig(jpaSpec, configPackage, jpaRepoElements);
//    }
//
//    private void generateSpringJpaConfig(String jpaSpec,
//                                         String configPackage,
//                                         Map<TypeElement, DeclaredType> jpaRepoElements) {
//        String jpaRepoPackages = jpaRepoElements.keySet()
//                .stream()
//                .map(typeElement -> processingEnv.getElementUtils()
//                        .getPackageOf(typeElement)
//                        .getQualifiedName()
//                        .toString())
//                .distinct()
//                .collect(Collectors.collectingAndThen(Collectors.toList(), this::writeRepositoriesBasePackages));
//        String entitiesPackages = jpaRepoElements.values()
//                .stream()
//                .map(declaredType -> declaredType.getTypeArguments().get(0))
//                .filter(DeclaredType.class::isInstance)
//                .map(DeclaredType.class::cast)
//                .map(DeclaredType::asElement)
//                .filter(TypeElement.class::isInstance)
//                .map(TypeElement.class::cast)
//                .map(typeElement -> processingEnv.getElementUtils()
//                        .getPackageOf(typeElement)
//                        .getQualifiedName()
//                        .toString())
//                .distinct()
//                .collect(Collectors.collectingAndThen(Collectors.toList(), this::writeEntityBasePackagesArray));
//        try {
//            generateJpaConfigurerClass(jpaSpec, configPackage, entitiesPackages, jpaRepoPackages);
//        } catch (IOException e) {
//            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
//        }
//    }
//
//    private void generateJpaConfigurerClass(String jpaSpec,
//                                            String basePackage,
//                                            String entityBasePackages,
//                                            String jpaReposBasePackages) throws IOException {
//        String className = basePackage + '.' + jpaSpec + "JpaSpecConfigurer";
//        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generating: " + className);
//        processingEnv.getMessager().printMessage(
//                Diagnostic.Kind.NOTE,
//                "Jpa Spec: %s Base package: %s".formatted(jpaSpec, basePackage)
//        );
//        Instant timestamp = Instant.now();
//        String generatedClass = readClasspathFile(JPA_SPEC_JPA_CONFIGURER_TEMPLATE_FILE);
//        generatedClass = generatedClass.replace("${BASE_PACKAGE}", basePackage);
//        generatedClass = generatedClass.replace(
//                "${ANNOTATION_PROCESSOR}",
//                EnableJpaSpecAnnotationProcessor.class.getCanonicalName()
//        );
//        String compiler = processingEnv.getSourceVersion() != null ? processingEnv.getSourceVersion().name() : "java";
//        generatedClass = generatedClass.replace("${COMPILER}", compiler);
//        generatedClass = generatedClass.replace("${JPA_SPEC}", jpaSpec);
//        generatedClass = generatedClass.replace("${TIMESTAMP}", ISO_INSTANT.format(timestamp));
//        generatedClass = generatedClass.replace("${ENTITY_BASE_PACKAGES}", entityBasePackages);
//        generatedClass = generatedClass.replace("${JPA_REPOS_BASE_PACKAGES}", jpaReposBasePackages);
//        generateClass(className, generatedClass);
//    }
//
//    private void generateClass(String generatedClassName, String generatedClass) throws IOException {
//        JavaFileObject classFile = processingEnv.getFiler().createSourceFile(generatedClassName);
//        try (Writer writer = classFile.openWriter()) {
//            writer.write(generatedClass);
//        }
//    }
//
//    private String writeRepositoriesBasePackages(Collection<String> repoBasePackages) {
//        Iterator<String> basePackagesIterator = repoBasePackages.iterator();
//        StringBuilder basePackageBuilder = new StringBuilder("{");
//        while (basePackagesIterator.hasNext()) {
//            String basePackage = basePackagesIterator.next();
//            basePackageBuilder.append('\"').append(basePackage).append('\"');
//            if (basePackagesIterator.hasNext()) {
//                basePackageBuilder.append(", ");
//            }
//        }
//        return basePackageBuilder.append("}").toString();
//    }
//
//    private String writeEntityBasePackagesArray(Collection<String> entityBasePackages) {
//        Iterator<String> basePackagesIterator = entityBasePackages.iterator();
//        StringBuilder basePackageBuilder = new StringBuilder("new String[]{");
//        while (basePackagesIterator.hasNext()) {
//            String basePackage = basePackagesIterator.next();
//            basePackageBuilder.append('\"').append(basePackage).append('\"');
//            if (basePackagesIterator.hasNext()) {
//                basePackageBuilder.append(", ");
//            }
//        }
//        return basePackageBuilder.append('}').toString();
//    }
//
//    private String readClasspathFile(String path) throws IOException {
//        FileObject fileObject = processingEnv.getFiler().getResource(CLASS_PATH, "", path);
//        return String.valueOf(fileObject.getCharContent(false));
//    }

}
