package com.olsonsolution.common.property.domain.port.stereotype;

import java.lang.reflect.Type;
import java.util.Set;

public interface PropertySpec {

    String getName();

    Type getType();

    Set<String> getEnums();

    String getDescription();

    boolean isRequired();

}
