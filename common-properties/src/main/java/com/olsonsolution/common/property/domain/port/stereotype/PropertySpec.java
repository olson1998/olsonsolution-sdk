package com.olsonsolution.common.property.domain.port.stereotype;

import java.util.Set;

public interface PropertySpec {

    String getName();

    Class<?> getType();

    Set<String> getEnums();

    String getDescription();

    boolean isRequired();

}
