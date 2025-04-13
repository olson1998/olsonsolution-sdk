package com.olsonsolution.common.spring.application.jpa.config;

import com.google.auto.service.AutoService;
import com.olsonsolution.common.spring.domain.model.annotation.JpaSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes("com.olsonsolution.common.spring.domain.model.annotation.JpaSpec")
public class JpaSpecAnnotationProcessor extends AbstractProcessor {

    private static final String TEMPLATE_FILE = "/src/main/resources/jpa/templates/_JpaConfigurerTemplate";

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
        String generatedClass;
        String jpaSpecValue = jpaSpec.value();
        String basePackage = processingEnv.getElementUtils()
                .getPackageOf(typeElement)
                .getQualifiedName()
                .toString();
        String className = basePackage + '.' + jpaSpecValue + "JpaConfigurer";
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generating: " + className);
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "Jpa Spec: %s Base package: %s".formatted(jpaSpecValue, basePackage)
        );
        generatedClass = CLASS_TEMPLATE.replace("${BASE_PACKAGE}", basePackage);
        generatedClass = generatedClass.replace("${JPA_SPEC}", jpaSpecValue);
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

//    private String readTemplateContent() throws IOException {
//        ClassLoader classLoader = JpaSpecAnnotationProcessor.class.getClassLoader();
//        try (InputStream input = classLoader.getResourceAsStream(TEMPLATE_FILE)) {
//            if (input != null) {
//                return new String(input.readAllBytes(), UTF_8);
//            } else {
//                throw new IOException("Null input stream for template file " + TEMPLATE_FILE);
//            }
//        }
//    }

    private static final String CLASS_TEMPLATE = """
            package ${BASE_PACKAGE};
            
            import com.olsonsolution.common.spring.domain.port.props.jpa.JpaProperties;
            import com.olsonsolution.common.spring.domain.port.props.jpa.JpaSpecProperties;
            import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
            import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlDataSourceProvider;
            import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
            import com.olsonsolution.common.spring.domain.port.repository.jpa.EntityManagerFactoryDelegate;
            import com.olsonsolution.common.spring.domain.port.repository.jpa.PlatformTransactionManagerDelegate;
            import com.olsonsolution.common.spring.domain.service.jpa.MultiVendorEntityManagerFactory;
            import com.olsonsolution.common.spring.domain.service.jpa.MultiVendorPlatformTransactionManager;
            import org.apache.commons.lang3.StringUtils;
            import org.springframework.context.annotation.Bean;
            import org.springframework.context.annotation.Configuration;
            
            @Configuration
            public class ${JPA_SPEC}JpaConfigurer {
            
                private static final String JPA_SPEC_NAME = "${JPA_SPEC}";
            
                private final EntityManagerFactoryDelegate entityManagerFactoryDelegate;
            
                private final PlatformTransactionManagerDelegate platformTransactionManagerDelegate;
            
                public ${JPA_SPEC}JpaConfigurer(JpaProperties jpaProperties,
                                      DataSourceSpecManager dataSourceSpecManager,
                                      SqlDataSourceProvider sqlDataSourceProvider,
                                      DestinationDataSourceManager destinationDataSourceManager) {
                    JpaSpecProperties specProperties = getJpaSpecProperties(jpaProperties);
                    this.entityManagerFactoryDelegate = createEntityManagerFactoryDelegate(
                            specProperties,
                            jpaProperties,
                            dataSourceSpecManager,
                            sqlDataSourceProvider,
                            destinationDataSourceManager
                    );
                    this.platformTransactionManagerDelegate = createPlatformTransactionManagerDelegate(
                            dataSourceSpecManager,
                            sqlDataSourceProvider,
                            entityManagerFactoryDelegate
                    );
                }
            
                @Bean(JPA_SPEC_NAME + "_entityManagerFactory")
                public EntityManagerFactoryDelegate entityManagerFactoryDelegate() {
                    return entityManagerFactoryDelegate;
                }
            
                @Bean(JPA_SPEC_NAME + "_platformTransactionManager")
                public PlatformTransactionManagerDelegate platformTransactionManagerDelegate() {
                    return platformTransactionManagerDelegate;
                }
            
                private static JpaSpecProperties getJpaSpecProperties(JpaProperties jpaProperties) {
                    return jpaProperties.getJpaSpecificationsProperties()
                            .stream()
                            .filter(props -> isConfigForName(props))
                            .findFirst()
                            .orElseThrow();
                }
            
                private static EntityManagerFactoryDelegate createEntityManagerFactoryDelegate(
                        JpaSpecProperties properties,
                        JpaProperties jpaProperties,
                        DataSourceSpecManager dataSourceSpecManager,
                        SqlDataSourceProvider sqlDataSourceProvider,
                        DestinationDataSourceManager destinationDataSourceManager) {
                    String schema = properties.getSchema();
                    String name = properties.getName();
                    return new MultiVendorEntityManagerFactory(
                            schema,
                            name,
                            jpaProperties,
                            dataSourceSpecManager,
                            sqlDataSourceProvider,
                            destinationDataSourceManager
                    );
                }
            
                private static PlatformTransactionManagerDelegate createPlatformTransactionManagerDelegate(
                        DataSourceSpecManager dataSourceSpecManager,
                        SqlDataSourceProvider sqlDataSourceProvider,
                        EntityManagerFactoryDelegate entityManagerFactoryDelegate) {
                    return new MultiVendorPlatformTransactionManager(
                            dataSourceSpecManager,
                            sqlDataSourceProvider,
                            entityManagerFactoryDelegate
                    );
                }
            
                private static boolean isConfigForName(JpaSpecProperties props) {
                    String value = props.getName() == null ? props.getSchema() : props.getName();
                    return StringUtils.equals(value, JPA_SPEC_NAME);
                }
            
            }
            
            """;

}
