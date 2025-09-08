package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import jakarta.persistence.Column;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

record ColumnElementMetadata(VariableElement fieldElement, TypeElement fieldTypeElement, Column columnAnnotation) {
}
