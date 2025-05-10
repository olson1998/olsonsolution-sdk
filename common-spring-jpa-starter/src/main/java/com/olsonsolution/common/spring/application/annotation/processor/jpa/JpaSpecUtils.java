package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.olsonsolution.common.spring.application.annotation.jpa.EnableJpaSpec;
import com.olsonsolution.common.spring.application.annotation.jpa.JpaSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Configuration;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.INTERFACE;

@RequiredArgsConstructor
class JpaSpecUtils {

    private static final Set<Class<? extends Annotation>> SPRING_CONFIG_ANNOTATION = Set.of(
            Configuration.class,
            AutoConfiguration.class
    );

    private final ProcessingEnvironment processingEnv;

    List<TypeElement> collectEnableJpaSpec(RoundEnvironment roundEnv) {
        return roundEnv.getElementsAnnotatedWith(EnableJpaSpec.class).stream()
                .filter(this::isSpringConfig)
                .filter(element -> element.getKind() == CLASS)
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .toList();
    }

    Map<String, Map<TypeElement, DeclaredType>> collectJpaSpecRepositories(RoundEnvironment roundEnv,
                                                                           List<TypeElement> enableJpaSpecElements) {
        List<String> jpaSpecEnabledPackages = collectJpaSpecEnabledPackages(enableJpaSpecElements);
        Map<String, List<String>> jpaSpecBasePackages =
                collectJpaSpecPackages(enableJpaSpecElements, jpaSpecEnabledPackages);
        List<TypeElement> jpaSpecElements = collectEnableJpaSpec(roundEnv);
        TypeElement jpaRepositoryElement = processingEnv.getElementUtils()
                .getTypeElement("org.springframework.data.jpa.repository.JpaRepository");
        Map<TypeElement, DeclaredType> jpaSpecElementJpaRepository = new HashMap<>();
        jpaSpecElements.forEach(jpaSpecElement -> processConfig(
                jpaSpecElement,
                jpaRepositoryElement,
                jpaSpecEnabledPackages,
                jpaSpecBasePackages,
                jpaSpecElementJpaRepository
        ));
        return jpaSpecElementJpaRepository
                .entrySet()
                .stream()
                .collect(Collectors.groupingBy(
                        jpaSpecElement -> jpaSpecElement.getKey()
                                .getAnnotation(JpaSpec.class)
                                .value(),
                        Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue)
                ));
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

    private List<TypeElement> collectJpaSpec(RoundEnvironment roundEnv) {
        return roundEnv.getElementsAnnotatedWith(JpaSpec.class).stream()
                .filter(element -> element.getKind() == INTERFACE)
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .toList();
    }

    private List<String> collectJpaSpecEnabledPackages(List<TypeElement> enableJpaSpecElements) {
        return enableJpaSpecElements.stream()
                .map(typeElement -> typeElement.getAnnotation(EnableJpaSpec.class))
                .filter(enableJpaSpec -> enableJpaSpec.basePackages().length == 0)
                .map(EnableJpaSpec::value)
                .toList();
    }

    private Map<String, List<String>> collectJpaSpecPackages(List<TypeElement> enableJpaSpecElements,
                                                             List<String> jpaSpecEnabledPackages) {
        return enableJpaSpecElements.stream()
                .map(typeElement -> typeElement.getAnnotation(EnableJpaSpec.class))
                .filter(enableJpaSpec -> enableJpaSpec.basePackages().length > 0)
                .filter(enableJpaSpec -> !jpaSpecEnabledPackages.contains(enableJpaSpec.value()))
                .collect(Collectors.groupingBy(EnableJpaSpec::value,
                        Collectors.collectingAndThen(Collectors.toList(), l -> l.stream()
                                .map(EnableJpaSpec::basePackages)
                                .flatMap(Arrays::stream)
                                .distinct()
                                .toList())));
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
