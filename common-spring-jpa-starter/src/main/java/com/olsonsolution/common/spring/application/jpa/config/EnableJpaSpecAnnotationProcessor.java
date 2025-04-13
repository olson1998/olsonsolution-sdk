package com.olsonsolution.common.spring.application.jpa.config;

import com.google.auto.service.AutoService;
import com.olsonsolution.common.spring.domain.model.annotation.EnableJpaSpec;
import com.olsonsolution.common.spring.domain.model.annotation.JpaSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes("com.olsonsolution.common.spring.domain.model.annotation.EnableJpaSpec")
public class EnableJpaSpecAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(EnableJpaSpec.class);
        for (Element element : elements) {
            EnableJpaSpec enableJpaSpec = element.getAnnotation(EnableJpaSpec.class);
            processElement(element, enableJpaSpec);
        }
        return true;
    }

    private void processElement(Element element, EnableJpaSpec enableJpaSpec) {
        for (JpaSpec jpaSpec : enableJpaSpec.value()) {
            if (element instanceof TypeElement typeElement) {
                processAnnotatedClass(typeElement, jpaSpec);
            }
        }
    }

    private void processAnnotatedClass(TypeElement typeElement, JpaSpec jpaSpec) {
        String basePackage = processingEnv.getElementUtils()
                .getPackageOf(typeElement)
                .getQualifiedName()
                .toString();
        generateJpaConfigurerClass(basePackage, jpaSpec);
        generateEnableJpaRepositoriesClass(basePackage, jpaSpec);
    }

    private void generateJpaConfigurerClass(String basePackage, JpaSpec jpaSpec) {
        String className = basePackage + '.' + jpaSpec.value() + "JpaConfigurer";
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generating: " + className);
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "Jpa Spec: %s Base package: %s".formatted(jpaSpec.value(), basePackage)
        );
        String generatedClass = JPA_CONFIGURER_CLASS_TEMPLATE.replace("${BASE_PACKAGE}", basePackage);
        generatedClass = generatedClass.replace("${JPA_SPEC}", jpaSpec.value());
        generateClass(className, generatedClass);
    }

    private void generateEnableJpaRepositoriesClass(String basePackage, JpaSpec jpaSpec) {
        String className = basePackage + ".Enable" + jpaSpec.value() + "JpaRepositoriesConfig";
        String repositoriesBasePackages = writeRepositoriesBasePackages(jpaSpec);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generating: " + className);
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "Jpa Spec: %s Base package: %s".formatted(jpaSpec.value(), basePackage)
        );
        String generatedClass = ENABLE_JPA_REPOSITORIES_CLASS_TEMPLATE.replace("${BASE_PACKAGE}", basePackage);
        generatedClass = generatedClass.replace("${JPA_REPOSITORIES_PACKAGES}", repositoriesBasePackages);
        generatedClass = generatedClass.replace("${JPA_SPEC}", jpaSpec.value());
        generateClass(className, generatedClass);
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

    private static final String ENABLE_JPA_REPOSITORIES_CLASS_TEMPLATE = """
            package ${BASE_PACKAGE};
            
            import org.springframework.context.annotation.Configuration;
            import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
            
            @EnableJpaRepositories(
                    basePackages = ${JPA_REPOSITORIES_PACKAGES},
                    transactionManagerRef = "${JPA_SPEC}_platformTransactionManager",
                    entityManagerFactoryRef = "${JPA_SPEC}_entityManagerFactory"
            )
            @Configuration
            public class Enable${JPA_SPEC}JpaRepositoriesConfig {
            }
            
            """;

    private static final String JPA_CONFIGURER_CLASS_TEMPLATE = """
            package ${BASE_PACKAGE};
            
            import com.olsonsolution.common.spring.application.jpa.config.JpaSpecConfigurer;
            import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
            import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlDataSourceProvider;
            import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
            import com.olsonsolution.common.spring.domain.port.repository.jpa.EntityManagerFactoryDelegate;
            import com.olsonsolution.common.spring.domain.port.repository.jpa.PlatformTransactionManagerDelegate;
            import org.springframework.beans.factory.annotation.Qualifier;
            import org.springframework.context.annotation.Bean;
            import org.springframework.context.annotation.Configuration;
            
            @Configuration
            public class ${JPA_SPEC}JpaConfigurer {
            
                private static final String JPA_SPEC_NAME = "${JPA_SPEC}";
            
                @Bean(JPA_SPEC_NAME + "_entityManagerFactory")
                public EntityManagerFactoryDelegate entityManagerFactoryDelegate(
                        JpaSpecConfigurer jpaSpecConfigurer,
                        DataSourceSpecManager dataSourceSpecManager,
                        SqlDataSourceProvider sqlDataSourceProvider,
                        DestinationDataSourceManager destinationDataSourceManager) {
                    return jpaSpecConfigurer.createEntityManagerFactoryDelegate(
                            JPA_SPEC_NAME,
                            dataSourceSpecManager,
                            sqlDataSourceProvider,
                            destinationDataSourceManager
                    );
                }
            
                @Bean(JPA_SPEC_NAME + "_platformTransactionManager")
                public PlatformTransactionManagerDelegate platformTransactionManagerDelegate(
                        JpaSpecConfigurer jpaSpecConfigurer,
                        DataSourceSpecManager dataSourceSpecManager,
                        SqlDataSourceProvider sqlDataSourceProvider,
                        @Qualifier(JPA_SPEC_NAME + "_entityManagerFactory") EntityManagerFactoryDelegate entityManagerFactory) {
                    return jpaSpecConfigurer.createPlatformTransactionManagerDelegate(
                            dataSourceSpecManager,
                            sqlDataSourceProvider,
                            entityManagerFactory
                    );
                }
            
            }
            
            """;

}
