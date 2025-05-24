package com.olsonsolution.common.reflection.domain.port.repository.annotion.processor;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Set;

public interface TypeElementUtils {

    Set<VariableElement> getDeclaredVariableElements(TypeElement typeElement, boolean includeSuperClass);

    TypeElement getFieldTypeElement(VariableElement fieldElement);

}
