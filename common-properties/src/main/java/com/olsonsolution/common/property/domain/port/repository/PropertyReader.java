package com.olsonsolution.common.property.domain.port.repository;

public interface PropertyReader<T> {

    Class<T> getPropertyType();

    T parse(String propertyValue);

}
