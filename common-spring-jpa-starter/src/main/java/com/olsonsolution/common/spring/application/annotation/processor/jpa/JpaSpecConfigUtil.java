package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.olsonsolution.common.reflection.domain.port.repository.annotion.processor.MessagePrinter;
import com.olsonsolution.common.spring.application.annotation.jpa.EnableJpaSpec;
import com.olsonsolution.common.spring.application.annotation.jpa.JpaSpec;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Configuration;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.INTERFACE;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class JpaSpecConfigUtil {

    private static final Set<Class<? extends Annotation>> SPRING_CONFIG_ANNOTATION = Set.of(
            Configuration.class,
            AutoConfiguration.class
    );

    private final Elements elementUtils;

    private final MessagePrinter messagePrinter;

    private final TableMetadataUtil tableMetadataUtil;

    private final ProcessingEnvironment processingEnv;

    List<JpaSpecMetadata> mirrorJpaSpecs(RoundEnvironment roundEnv) {
        Map<String, TypeElement> enableJpaSpecElements = mirrorEnableJpaSpec(roundEnv);
        Map<String, JpaSpecMetadata.Builder> configs = new HashMap<>(enableJpaSpecElements.size());
        List<String> jpaSpecEnabledPackages = collectJpaSpecEnabledPackages(enableJpaSpecElements.values());
        Map<String, List<String>> jpaSpecBasePackages =
                collectJpaSpecPackages(enableJpaSpecElements.values(), jpaSpecEnabledPackages);
        List<TypeElement> jpaSpecRepositories = collectJpaSpecRepositories(roundEnv);
        jpaSpecRepositories.forEach(jpaRepositoryType -> processJpaSpecRepository(
                jpaRepositoryType,
                jpaSpecEnabledPackages,
                jpaSpecBasePackages,
                enableJpaSpecElements,
                configs
        ));
        return configs.values()
                .stream()
                .map(metadata -> metadata.build(elementUtils))
                .toList();
    }

    private Map<String, TypeElement> mirrorEnableJpaSpec(RoundEnvironment roundEnv) {
        return roundEnv.getElementsAnnotatedWith(EnableJpaSpec.class).stream()
                .filter(this::isSpringConfig)
                .filter(element -> element.getKind() == CLASS)
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .map(element -> new DefaultMapEntry<>(
                        element.getAnnotation(EnableJpaSpec.class).value(),
                        element
                )).collect(Collectors.toUnmodifiableMap(DefaultMapEntry::getKey, DefaultMapEntry::getValue));
    }

    private void processJpaSpecRepository(TypeElement jpaRepositoryType,
                                          List<String> jpaSpecAllPackagesScan,
                                          Map<String, List<String>> jpaSpecBasePackagesScan,
                                          Map<String, TypeElement> enableJpaSpecElements,
                                          Map<String, JpaSpecMetadata.Builder> configs) {
        JpaSpec jpaSpec = jpaRepositoryType.getAnnotation(JpaSpec.class);
        String jpaSpecName = jpaSpec.value();
        TypeElement enableJpaSpecElement = enableJpaSpecElements.get(jpaSpecName);
        if (enableJpaSpecElement == null) {
            messagePrinter.print(
                    Diagnostic.Kind.ERROR, JpaSpecConfigUtil.class,
                    "Jpa Spec %s is not enabled".formatted(jpaSpecName)
            );
            return;
        }
        JpaSpecMetadata.Builder config = configs.computeIfAbsent(
                jpaSpecName,
                name -> JpaSpecMetadata.builder().jpaSpec(name).enableJpaSpecElement(enableJpaSpecElement)
        );
        DeclaredType jpaRepoType = jpaRepositoryType.getInterfaces()
                .stream()
                .filter(DeclaredType.class::isInstance)
                .map(DeclaredType.class::cast)
                .findFirst()
                .orElse(null);
        if (jpaRepoType == null) {
            messagePrinter.print(
                    Diagnostic.Kind.ERROR,
                    JpaSpecConfigUtil.class,
                    "Class %s annotated with @JpaSpec is not instance of Jpa Repository"
                            .formatted(jpaRepositoryType.getQualifiedName())
            );
        } else {
            List<? extends TypeMirror> jpaRepoArgs = jpaRepoType.getTypeArguments();
            if (jpaRepoArgs == null || jpaRepoArgs.size() != 2) {
                messagePrinter.print(
                        Diagnostic.Kind.ERROR,
                        JpaSpecConfigUtil.class,
                        "Jpa Repository %s annotated with @JpaSpec miss configuration"
                                .formatted(jpaRepositoryType.getQualifiedName())
                );
            } else if (jpaRepoArgs.get(0) instanceof DeclaredType entityDeclaredType &&
                    entityDeclaredType.asElement() instanceof TypeElement entityType) {
                if (jpaSpecAllPackagesScan.contains(jpaSpecName) ||
                        isInEnabledPackage(jpaSpecName, entityType, jpaSpecBasePackagesScan)) {
                    String table = tableMetadataUtil.getTableName(entityType);
                    config.appendJpaRepository(entityType, jpaRepositoryType, table);
                }
            }
        }
    }

    private List<TypeElement> collectJpaSpecRepositories(RoundEnvironment roundEnv) {
        TypeElement jpaRepositoryElement = processingEnv.getElementUtils()
                .getTypeElement("org.springframework.data.jpa.repository.JpaRepository");
        TypeMirror jpaRepositoryType = processingEnv.getTypeUtils().erasure(jpaRepositoryElement.asType());
        return roundEnv.getElementsAnnotatedWith(JpaSpec.class).stream()
                .filter(element -> element.getKind() == INTERFACE)
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .filter(jpaSpecType -> isInstanceOfJpaRepo(jpaSpecType, jpaRepositoryType))
                .toList();
    }

    private List<String> collectJpaSpecEnabledPackages(Collection<TypeElement> enableJpaSpecElements) {
        return enableJpaSpecElements.stream()
                .map(typeElement -> typeElement.getAnnotation(EnableJpaSpec.class))
                .filter(enableJpaSpec -> enableJpaSpec.basePackages().length == 0)
                .map(EnableJpaSpec::value)
                .toList();
    }

    private Map<String, List<String>> collectJpaSpecPackages(Collection<TypeElement> enableJpaSpecElements,
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

    private boolean isInstanceOfJpaRepo(TypeElement jpaSpecElement, TypeMirror jpaRepositoryType) {
        for (TypeMirror iface : jpaSpecElement.getInterfaces()) {
            if (processingEnv.getTypeUtils()
                    .isSameType(processingEnv.getTypeUtils().erasure(iface), jpaRepositoryType)) {
                return true;
            }
            if (iface instanceof TypeElement ifaceElement) {
                if (isInstanceOfJpaRepo(ifaceElement, jpaRepositoryType)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInEnabledPackage(String jpaSpec,
                                       TypeElement entityTypeElement,
                                       Map<String, List<String>> enabledBasePackages) {
        String targetPackage = processingEnv.getElementUtils().getPackageOf(entityTypeElement).toString();
        return enabledBasePackages.entrySet()
                .stream()
                .filter(jpaSpecBasePackages -> jpaSpecBasePackages.getKey().equals(jpaSpec))
                .flatMap(jpaSpecBasePackages -> jpaSpecBasePackages.getValue().stream())
                .anyMatch(enabledBasePackage -> isInsidePackage(targetPackage, enabledBasePackage));
    }

    private boolean isInsidePackage(String basePackage, String targetPackage) {
        return targetPackage.startsWith(basePackage);
    }

    private boolean isSpringConfig(Element element) {
        return SPRING_CONFIG_ANNOTATION.stream()
                .anyMatch(annotation -> element.getAnnotation(annotation) != null);
    }

}


