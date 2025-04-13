package com.olsonsolution.common.spring.application.jpa.config;

import com.olsonsolution.common.spring.domain.model.annotation.JpaSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Set;

@SupportedAnnotationTypes("com.olsonsolution.common.spring.domain.model.annotation.JpaSpec")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class JpaSpecAnnotationProcessor extends AbstractProcessor {

    private static final File CONFIGURER_CLASS_TEMPLATE =
            new File("src/main/resources/jpa/templates/_JpaConfigurerTemplate");

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(JpaSpec.class);
        for (Element element : elements) {
            JpaSpec jpaSpec = element.getAnnotation(JpaSpec.class);
            if (element instanceof TypeElement typeElement) {
                processAnnotatedClass(typeElement, jpaSpec);
            }
        }
        return true;
    }

    private void processAnnotatedClass(TypeElement typeElement, JpaSpec jpaSpec) {
        try {
            String generatedClass;
            String jpaSpecValue = jpaSpec.value();
            String basePackage = processingEnv.getElementUtils()
                    .getPackageOf(typeElement)
                    .getQualifiedName()
                    .toString();
            String className = basePackage + '.' + jpaSpecValue + "JpaConfigurer";
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generating: " + className);
            String template = Files.readString(CONFIGURER_CLASS_TEMPLATE.toPath());
            generatedClass = template.replace("${BASE_PACKAGE}", basePackage);
            generatedClass = generatedClass.replace("${JPA_SPEC}", jpaSpecValue);
            generateClass(className, generatedClass);
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    private void generateClass(String generatedClassName, String generatedClass) {
        try {
            JavaFileObject classFile = processingEnv.getFiler().createSourceFile(generatedClassName);
            try (Writer writer = classFile.openWriter()) {
                writer.write(generatedClass);
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

}
