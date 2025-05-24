package com.olsonsolution.common.reflection.domain.service.annotion.processor;

import com.olsonsolution.common.reflection.domain.port.repository.annotion.processor.TypeElementUtils;
import lombok.RequiredArgsConstructor;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.lang.model.element.ElementKind.FIELD;

@RequiredArgsConstructor
public class TypeElementUtilityService implements TypeElementUtils {

    private final Types typeUtils;

    private final Elements elementUtils;

    @Override
    public Set<VariableElement> getDeclaredVariableElements(TypeElement typeElement, boolean includeSuperClass) {
        Stream.Builder<VariableElement> fields = Stream.builder();
        collectDeclaredVariableElements(typeElement, fields, includeSuperClass);
        return fields.build().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public TypeElement getClassElement(TypeMirror typeMirror) {
        if (typeMirror instanceof DeclaredType &&
                typeUtils.asElement(typeMirror) instanceof TypeElement classElement) {
            return classElement;
        } else {
            return null;
        }
    }

    @Override
    public TypeElement getFieldTypeElement(VariableElement fieldElement) {
        if (fieldElement.getKind() == FIELD && typeUtils.asElement(fieldElement.asType())
                instanceof TypeElement fieldTypeElement) {
            return fieldTypeElement;
        } else {
            throw new IllegalArgumentException("Field element is not a field");
        }
    }

    private void collectDeclaredVariableElements(TypeElement typeElement, Stream.Builder<VariableElement> fields,
                                                 boolean includeSuperClass) {
        typeElement.getEnclosedElements()
                .stream()
                .filter(element -> element.getKind() == FIELD)
                .filter(VariableElement.class::isInstance)
                .map(VariableElement.class::cast)
                .forEach(fields::add);
        if (includeSuperClass) {
            TypeMirror superClassMirror = typeElement.getSuperclass();
            TypeElement objectClassElement = elementUtils.getTypeElement("java.lang.Object");
            TypeMirror objectClassMirror = objectClassElement.asType();
            boolean isObjectClass = typeUtils.isSameType(
                    typeUtils.erasure(superClassMirror),
                    typeUtils.erasure(objectClassMirror)
            );
            TypeElement superClassElement = getClassElement(superClassMirror);
            if (superClassElement != null && !isObjectClass) {
                collectDeclaredVariableElements(superClassElement, fields, true);
            }
        }
    }

}
