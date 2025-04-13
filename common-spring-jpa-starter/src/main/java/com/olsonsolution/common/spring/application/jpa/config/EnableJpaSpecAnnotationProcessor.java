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
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static javax.tools.StandardLocation.CLASS_PATH;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes("com.olsonsolution.common.spring.domain.model.annotation.EnableJpaSpec")
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
        roundEnv.getElementsAnnotatedWith(EnableJpaSpec.class)
                .stream()
                .filter(this::isSpringConfig)
                .forEach(this::processElement);
        return true;
    }

    private void processElement(Element element) {
        EnableJpaSpec enableJpaSpec = element.getAnnotation(EnableJpaSpec.class);
        for (JpaSpec jpaSpec : enableJpaSpec.value()) {
            if (element instanceof TypeElement typeElement) {
                try {
                    processAnnotatedClass(typeElement, jpaSpec);
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                }
            }
        }
    }

    private void processAnnotatedClass(TypeElement typeElement, JpaSpec jpaSpec) throws IOException {
        String basePackage = processingEnv.getElementUtils()
                .getPackageOf(typeElement)
                .getQualifiedName()
                .toString();
        generateJpaConfigurerClass(basePackage, jpaSpec);
        generateEnableJpaRepositoriesClass(basePackage, jpaSpec);
    }

    private void generateJpaConfigurerClass(String basePackage, JpaSpec jpaSpec) throws IOException {
        String className = basePackage + '.' + jpaSpec.value() + "JpaConfigurer";
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generating: " + className);
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "Jpa Spec: %s Base package: %s".formatted(jpaSpec.value(), basePackage)
        );
        Instant timestamp = Instant.now();
        String generatedClass = readClasspathFile(JPA_SPEC_JPA_CONFIGURER_TEMPLATE_FILE);
        generatedClass = generatedClass.replace("${BASE_PACKAGE}", basePackage);
        generatedClass = generatedClass.replace("${JPA_SPEC}", jpaSpec.value());
        generatedClass = generatedClass.replace("${TIMESTAMP}", timestamp.toString());
        generatedClass = generatedClass.replace("${EPOCH_MILLI}", String.valueOf(timestamp.toEpochMilli()));
        generateClass(className, generatedClass);
    }

    private void generateEnableJpaRepositoriesClass(String basePackage, JpaSpec jpaSpec) throws IOException {
        String className = basePackage + ".Enable" + jpaSpec.value() + "JpaRepositoriesConfig";
        String repositoriesBasePackages = writeRepositoriesBasePackages(jpaSpec);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generating: " + className);
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "Jpa Spec: %s Base package: %s".formatted(jpaSpec.value(), basePackage)
        );
        Instant timestamp = Instant.now();
        String generatedClass = readClasspathFile(ENABLE_JPA_SPEC_JPA_REPOSITORIES_TEMPLATE_FILE);
        generatedClass = generatedClass.replace("${BASE_PACKAGE}", basePackage);
        generatedClass = generatedClass.replace("${JPA_REPOSITORIES_PACKAGES}", repositoriesBasePackages);
        generatedClass = generatedClass.replace("${JPA_SPEC}", jpaSpec.value());
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

    private String writeRepositoriesBasePackages(JpaSpec jpaSpec) {
        List<String> basePackages = Arrays.asList(jpaSpec.repositoriesPackages());
        Iterator<String> basePackagesIterator = basePackages.iterator();
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

    private String readClasspathFile(String path) throws IOException {
        FileObject fileObject = processingEnv.getFiler().getResource(CLASS_PATH, "", path);
        return String.valueOf(fileObject.getCharContent(false));
    }

    private boolean isSpringConfig(Element element) {
        return SPRING_CONFIG_ANNOTATION.stream()
                .anyMatch(annotation -> element.getAnnotation(annotation) != null);
    }

}
