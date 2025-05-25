package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.olsonsolution.common.reflection.domain.port.repository.annotion.processor.TypeElementUtils;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class JpaEntityUtil {

    private final ProcessingEnvironment processingEnv;

    private final TypeElementUtils typeElementUtils;

    Set<VariableElement> getEntityClassFields(TypeElement typeElement) {
        Stream.Builder<VariableElement> fields = Stream.builder();
        collectEntityFieldElements(typeElement, fields);
        return fields.build().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    String getTableName(Element entityElement) {
        return entityElement.getAnnotation(Table.class) == null ?
                entityElement.getSimpleName().toString() :
                entityElement.getAnnotation(Table.class).name();
    }

    String getColumnName(VariableElement entityFieldElement) {
        return entityFieldElement.getAnnotation(Column.class) == null ?
                entityFieldElement.getSimpleName().toString() :
                entityFieldElement.getAnnotation(Column.class).name();
    }

    boolean isEmbeddable(VariableElement variableElement) {
        if (processingEnv.getTypeUtils().asElement(variableElement.asType()) instanceof TypeElement fieldTypeElement) {
            return fieldTypeElement.getAnnotation(Embeddable.class) != null;
        }
        return false;
    }

    boolean isIdentifier(VariableElement entityFieldElement) {
        return entityFieldElement.getAnnotation(Id.class) != null;
    }

    boolean isEmbeddableIdentifier(VariableElement entityFieldElement) {
        return entityFieldElement.getAnnotation(EmbeddedId.class) != null;
    }

    private void collectEntityFieldElements(TypeElement typeElement,
                                            Stream.Builder<VariableElement> fields) {
        typeElementUtils.getDeclaredVariableElements(typeElement, false)
                .forEach(fields::add);
        TypeElement mappedSuperClassElement = null;
        TypeMirror superClassMirror = typeElement.getSuperclass();
        if (superClassMirror != null && superClassMirror.getKind() != TypeKind.NONE) {
            mappedSuperClassElement = typeElementUtils.getClassElement(superClassMirror);
        }
        if (mappedSuperClassElement != null && mappedSuperClassElement.getAnnotation(MappedSuperclass.class) != null) {
            collectEntityFieldElements(mappedSuperClassElement, fields);
        }
    }
}
