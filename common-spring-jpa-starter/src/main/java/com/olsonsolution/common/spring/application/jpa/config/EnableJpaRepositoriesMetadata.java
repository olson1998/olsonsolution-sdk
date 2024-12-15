package com.olsonsolution.common.spring.application.jpa.config;

import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;

import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;
import static org.springframework.data.repository.query.QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND;

public class EnableJpaRepositoriesMetadata implements AnnotationMetadata {

    private final Map<String, Object> annotationAttributes;

    public EnableJpaRepositoriesMetadata(String entityManagerFactoryRef,
                                         String transactionManagerRef,
                                         Set<String> jpaRepoPackagesScan) {
        String[] basePackages = jpaRepoPackagesScan.toArray(String[]::new);
        this.annotationAttributes = Map.ofEntries(
                entry("basePackageClasses", new Class[0]),
                entry("basePackages", basePackages),
                entry("entityManagerFactoryRef", entityManagerFactoryRef),
                entry("transactionManagerFactoryRef", transactionManagerRef),
                entry("enableDefaultTransactions", true),
                entry("considerNestedRepositories", true),
                entry("bootstrapMode", BootstrapMode.DEFAULT),
                entry("excludeFilters", new AnnotationAttributes[0]),
                entry("includeFilters", new AnnotationAttributes[0]),
                entry("queryLookupStrategy", CREATE_IF_NOT_FOUND),
                entry("repositoryBaseClass", DefaultRepositoryBaseClass.class),
                entry("repositoryFactoryBeanClass", JpaRepositoryFactoryBean.class),
                entry("repositoryImplementationPostfix", "Impl"),
                entry("value", basePackages),
                entry("escapeCharacter", '\\'),
                entry("type", FilterType.ANNOTATION),
                entry("namedQueriesLocation", "")
        );
    }

    @Override
    public Map<String, Object> getAnnotationAttributes(String annotationName) {
        return annotationAttributes;
    }

    @Override
    public Set<MethodMetadata> getAnnotatedMethods(String annotationName) {
        return Set.of();
    }

    @Override
    public Set<MethodMetadata> getDeclaredMethods() {
        return Set.of();
    }

    @Override
    public MergedAnnotations getAnnotations() {
        return null;
    }

    @Override
    public String getClassName() {
        return EnableJpaRepositoriesMetadata.class.getCanonicalName();
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public boolean isAnnotation() {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public boolean isIndependent() {
        return false;
    }

    @Override
    public String getEnclosingClassName() {
        return "";
    }

    @Override
    public String getSuperClassName() {
        return "";
    }

    @Override
    public String[] getInterfaceNames() {
        return new String[0];
    }

    @Override
    public String[] getMemberClassNames() {
        return new String[0];
    }
}
