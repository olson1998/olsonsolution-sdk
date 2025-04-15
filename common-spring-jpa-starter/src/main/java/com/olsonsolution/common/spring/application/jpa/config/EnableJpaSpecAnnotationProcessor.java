package com.olsonsolution.common.spring.application.jpa.config;

import com.google.auto.service.AutoService;
import com.olsonsolution.common.spring.domain.model.annotation.EnableJpaSpec;
import com.olsonsolution.common.spring.domain.model.annotation.JpaSpec;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Configuration;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.tools.StandardLocation.CLASS_PATH;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({
        "com.olsonsolution.common.spring.domain.model.annotation.JpaSpec",
        "com.olsonsolution.common.spring.domain.model.annotation.EnableJpaSpec"
})
public class EnableJpaSpecAnnotationProcessor extends AbstractProcessor {

    private static final Set<Class<? extends Annotation>> SPRING_CONFIG_ANNOTATION = Set.of(
            Configuration.class,
            AutoConfiguration.class
    );
    private static final String ENABLE_JPA_SPEC_JPA_REPOSITORIES_TEMPLATE_FILE =
            "jpa/templates/_EnableJpaSpecJpaRepositories.template";
    private static final String JPA_SPEC_JPA_CONFIGURER_TEMPLATE_FILE =
            "jpa/templates/_JpaSpecJpaConfigurer.template";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<TypeElement> enableJpaSpecElements = roundEnv.getElementsAnnotatedWith(EnableJpaSpec.class).stream()
                .filter(this::isSpringConfig)
                .filter(element -> element.getKind() == CLASS)
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .toList();
        List<String> jpaSpecAllPackages = enableJpaSpecElements.stream()
                .map(typeElement -> typeElement.getAnnotation(EnableJpaSpec.class))
                .filter(enableJpaSpec -> enableJpaSpec.basePackages().length == 0)
                .map(EnableJpaSpec::value)
                .toList();
        Map<String, List<String>> jpaSpecBasePackages = enableJpaSpecElements.stream()
                .map(typeElement -> typeElement.getAnnotation(EnableJpaSpec.class))
                .filter(enableJpaSpec -> enableJpaSpec.basePackages().length > 0)
                .filter(enableJpaSpec -> !jpaSpecAllPackages.contains(enableJpaSpec.value()))
                .collect(Collectors.groupingBy(EnableJpaSpec::value,
                        Collectors.collectingAndThen(Collectors.toList(), l -> l.stream()
                                .map(EnableJpaSpec::basePackages)
                                .flatMap(Arrays::stream)
                                .distinct()
                                .toList())));
        Messager messager = processingEnv.getMessager();
        List<TypeElement> jpaSpecElements = roundEnv.getElementsAnnotatedWith(JpaSpec.class).stream()
                .filter(element -> element.getKind() == INTERFACE)
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .toList();
        messager.printMessage(Diagnostic.Kind.NOTE, "Enable JpaSpec: " + enableJpaSpecElements);
        messager.printMessage(Diagnostic.Kind.NOTE, "JpaSpec Repositories: " + jpaSpecElements);
        messager.printMessage(Diagnostic.Kind.NOTE, "Jpa Spec all packages scan config: " + jpaSpecAllPackages);
        messager.printMessage(Diagnostic.Kind.NOTE, "Jpa Spec packages scan config: " + jpaSpecBasePackages);
        if (!enableJpaSpecElements.isEmpty()) {
            processConfig(enableJpaSpecElements, jpaSpecElements, jpaSpecAllPackages, jpaSpecBasePackages);
        }
        return true;
    }

    private void processConfig(List<TypeElement> enableJpaSpecElements,
                               List<TypeElement> jpaSpecElements,
                               List<String> jpaSpecAllPackages,
                               Map<String, List<String>> jpaSpecBasePackages) {
        TypeElement jpaRepositoryElement = processingEnv.getElementUtils()
                .getTypeElement("org.springframework.data.jpa.repository.JpaRepository");
        Map<TypeElement, DeclaredType> jpaSpecElementJpaRepository = new HashMap<>();
        jpaSpecElements.forEach(jpaSpecElement -> processConfig(
                jpaSpecElement,
                jpaRepositoryElement,
                jpaSpecAllPackages,
                jpaSpecBasePackages,
                jpaSpecElementJpaRepository
        ));
        Map<String, Map<TypeElement, DeclaredType>> jpaSpecConfig = jpaSpecElementJpaRepository
                .entrySet()
                .stream()
                .collect(Collectors.groupingBy(
                        jpaSpecElement -> jpaSpecElement.getKey()
                                .getAnnotation(JpaSpec.class)
                                .value(),
                        Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue)
                ));
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "Jpa Spec Config: " + jpaSpecElementJpaRepository
        );
        jpaSpecConfig.forEach((jpaSpec, jpaRepoElements) -> generateSpringJpaConfig(
                jpaSpec,
                jpaRepoElements,
                enableJpaSpecElements
        ));
    }

    private void generateSpringJpaConfig(String jpaSpec,
                                         Map<TypeElement, DeclaredType> jpaRepoElements,
                                         List<TypeElement> enableJpaSpecElements) {
        if (enableJpaSpecElements.isEmpty()) {
            return;
        }
        String configPackage = processingEnv.getElementUtils()
                .getPackageOf(enableJpaSpecElements.get(0))
                .getQualifiedName().toString();
        if (enableJpaSpecElements.size() > 1) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "Multiple @EnableJpaSpec declared. Using: " + configPackage
            );
        }
        generateSpringJpaConfig(jpaSpec, configPackage, jpaRepoElements);
    }

    private void generateSpringJpaConfig(String jpaSpec,
                                         String configPackage,
                                         Map<TypeElement, DeclaredType> jpaRepoElements) {
        String jpaRepoPackages = jpaRepoElements.keySet()
                .stream()
                .map(typeElement -> processingEnv.getElementUtils()
                        .getPackageOf(typeElement)
                        .getQualifiedName()
                        .toString())
                .distinct()
                .collect(Collectors.collectingAndThen(Collectors.toList(), this::writeRepositoriesBasePackages));
        String entitiesPackages = jpaRepoElements.values()
                .stream()
                .map(declaredType -> declaredType.getTypeArguments().get(0))
                .filter(DeclaredType.class::isInstance)
                .map(DeclaredType.class::cast)
                .map(DeclaredType::asElement)
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .map(typeElement -> processingEnv.getElementUtils()
                        .getPackageOf(typeElement)
                        .getQualifiedName()
                        .toString())
                .distinct()
                .collect(Collectors.collectingAndThen(Collectors.toList(), this::writeEntityBasePackagesArray));
        try {
            generateJpaConfigurerClass(jpaSpec, configPackage, entitiesPackages);
            generateEnableJpaRepositoriesClass(jpaSpec, configPackage, jpaRepoPackages);
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    private void processConfig(TypeElement jpaSpecElement,
                               TypeElement jpaRepositoryElement,
                               List<String> jpaSpecAllPackagesScan,
                               Map<String, List<String>> jpaSpecBasePackagesScan,
                               Map<TypeElement, DeclaredType> jpaSpecElementJpaRepository) {
        JpaSpec jpaSpec = jpaSpecElement.getAnnotation(JpaSpec.class);
        if (isInstanceOfJpaRepo(jpaSpecElement, processingEnv.getTypeUtils().erasure(jpaRepositoryElement.asType()))) {
            DeclaredType jpaRepoType = jpaSpecElement.getInterfaces()
                    .stream()
                    .filter(DeclaredType.class::isInstance)
                    .map(DeclaredType.class::cast)
                    .findFirst()
                    .orElse(null);
            if (jpaRepoType == null) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Class %s annotated with @JpaSpec is not instance of Jpa Repository"
                                .formatted(jpaSpecElement.getQualifiedName())
                );
            } else {
                List<? extends TypeMirror> jpaRepoArgs = jpaRepoType.getTypeArguments();
                if (jpaRepoArgs == null || jpaRepoArgs.size() != 2) {
                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            "Jpa Repository %s annotated with @JpaSpec miss configuration"
                                    .formatted(jpaSpecElement.getQualifiedName())
                    );
                } else if (jpaSpecAllPackagesScan.contains(jpaSpec.value()) || isInEnabledPackage(
                        jpaSpec.value(),
                        jpaRepoArgs.get(0),
                        jpaSpecBasePackagesScan)) {
                    jpaSpecElementJpaRepository.put(jpaSpecElement, jpaRepoType);
                }
            }
        }
    }

    private void generateJpaConfigurerClass(String jpaSpec,
                                            String basePackage,
                                            String entityBasePackages) throws IOException {
        String className = basePackage + '.' + jpaSpec + "JpaConfigurer";
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generating: " + className);
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "Jpa Spec: %s Base package: %s".formatted(jpaSpec, basePackage)
        );
        Instant timestamp = Instant.now();
        String generatedClass = readClasspathFile(JPA_SPEC_JPA_CONFIGURER_TEMPLATE_FILE);
        generatedClass = generatedClass.replace("${BASE_PACKAGE}", basePackage);
        generatedClass = generatedClass.replace("${JPA_SPEC}", jpaSpec);
        generatedClass = generatedClass.replace("${TIMESTAMP}", timestamp.toString());
        generatedClass = generatedClass.replace("${EPOCH_MILLI}", String.valueOf(timestamp.toEpochMilli()));
        generatedClass = generatedClass.replace("${ENTITY_BASE_PACKAGES}", entityBasePackages);
        generateClass(className, generatedClass);
    }

    private void generateEnableJpaRepositoriesClass(String jpaSpec,
                                                    String basePackage,
                                                    String repositoriesBasePackages) throws IOException {
        String className = basePackage + ".Enable" + jpaSpec + "JpaRepositoriesConfig";
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generating: " + className);
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "Jpa Spec: %s Base package: %s".formatted(jpaSpec, basePackage)
        );
        Instant timestamp = Instant.now();
        String generatedClass = readClasspathFile(ENABLE_JPA_SPEC_JPA_REPOSITORIES_TEMPLATE_FILE);
        generatedClass = generatedClass.replace("${BASE_PACKAGE}", basePackage);
        generatedClass = generatedClass.replace("${REPO_BASE_PACKAGES}", repositoriesBasePackages);
        generatedClass = generatedClass.replace("${JPA_SPEC}", jpaSpec);
        generatedClass = generatedClass.replace("${TIMESTAMP}", timestamp.toString());
        generatedClass = generatedClass.replace("${EPOCH_MILLI}", String.valueOf(timestamp.toEpochMilli()));
        generateClass(className, generatedClass);
    }

    private void generateClass(String generatedClassName, String generatedClass) throws IOException {
        JavaFileObject classFile = processingEnv.getFiler().createSourceFile(generatedClassName);
        try (Writer writer = classFile.openWriter()) {
            writer.write(generatedClass);
        }
    }

    private String writeRepositoriesBasePackages(Collection<String> repoBasePackages) {
        Iterator<String> basePackagesIterator = repoBasePackages.iterator();
        StringBuilder basePackageBuilder = new StringBuilder("{");
        while (basePackagesIterator.hasNext()) {
            String basePackage = basePackagesIterator.next();
            basePackageBuilder.append('\"').append(basePackage).append('\"');
            if (basePackagesIterator.hasNext()) {
                basePackageBuilder.append(", ");
            }
        }
        return basePackageBuilder.append("}").toString();
    }

    private String writeEntityBasePackagesArray(Collection<String> entityBasePackages) {
        Iterator<String> basePackagesIterator = entityBasePackages.iterator();
        StringBuilder basePackageBuilder = new StringBuilder("new String[]{");
        while (basePackagesIterator.hasNext()) {
            String basePackage = basePackagesIterator.next();
            basePackageBuilder.append('\"').append(basePackage).append('\"');
            if (basePackagesIterator.hasNext()) {
                basePackageBuilder.append(", ");
            }
        }
        return basePackageBuilder.append("}").toString();
    }

    private String readClasspathFile(String path) throws IOException {
        FileObject fileObject = processingEnv.getFiler().getResource(CLASS_PATH, "", path);
        return String.valueOf(fileObject.getCharContent(false));
    }

    private boolean isInstanceOfJpaRepo(TypeElement jpaSpecElement,
                                        TypeMirror jpaRepoErased) {
        for (TypeMirror iface : jpaSpecElement.getInterfaces()) {
            if (processingEnv.getTypeUtils()
                    .isSameType(processingEnv.getTypeUtils().erasure(iface), jpaRepoErased)) {
                return true;
            }
            if (iface instanceof TypeElement ifaceElement) {
                if (isInstanceOfJpaRepo(ifaceElement, jpaRepoErased)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInEnabledPackage(String jpaSpec,
                                       TypeMirror entityTypeMirror,
                                       Map<String, List<String>> enabledBasePackages) {
        if (entityTypeMirror instanceof DeclaredType entityDeclaredType &&
                entityDeclaredType.asElement() instanceof TypeElement entityTypeElement) {
            String targetPackage = processingEnv.getElementUtils().getPackageOf(entityTypeElement).toString();
            return enabledBasePackages.entrySet()
                    .stream()
                    .filter(jpaSpecBasePackages -> jpaSpecBasePackages.getKey().equals(jpaSpec))
                    .flatMap(jpaSpecBasePackages -> jpaSpecBasePackages.getValue().stream())
                    .anyMatch(enabledBasePackage -> isInsidePackage(targetPackage, enabledBasePackage));
        } else {
            return false;
        }
    }

    private boolean isInsidePackage(String basePackage, String targetPackage) {
        return targetPackage.startsWith(basePackage);
    }

    private boolean isSpringConfig(Element element) {
        return SPRING_CONFIG_ANNOTATION.stream()
                .anyMatch(annotation -> element.getAnnotation(annotation) != null);
    }

}
