package com.olsonsolution.common.spring.application.datasource.migration.annotation.processor;

import com.google.auto.service.AutoService;
import com.olsonsolution.common.spring.application.datasource.migration.annotation.ChangeSetEntity;
import jakarta.persistence.Entity;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Map.entry;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({
        "com.olsonsolution.common.spring.application.datasource.migration.annotation.ChangeSetEntity"
})
public class LiquibaseEntityAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<Element, ChangeSetEntity> changeSetEntities = getJpaEntities(roundEnv);
        return true;
    }

    private void processChangeSetEntity(Element element, ChangeSetEntity changeSetEntity, RoundEnvironment roundEnv) {
        String changeLogPath = changeSetEntity.path();

    }

    private Map<Element, ChangeSetEntity> getJpaEntities(RoundEnvironment roundEnv) {
        return roundEnv.getElementsAnnotatedWith(ChangeSetEntity.class)
                .stream()
                .filter(this::isJpaEntity)
                .map(element -> entry(element, element.getAnnotation(ChangeSetEntity.class)))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private boolean isJpaEntity(Element element) {
        return element.getAnnotation(Entity.class) != null;
    }

}
