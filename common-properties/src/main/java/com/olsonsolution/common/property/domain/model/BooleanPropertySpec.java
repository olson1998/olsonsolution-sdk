package com.olsonsolution.common.property.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BooleanPropertySpec extends PropertySpecModel {

    public static final Set<String> ENUMS = Set.of("true", "false");

    @Builder(builderMethodName = "booleanPropertySpec")
    public BooleanPropertySpec(String name, String description, boolean required) {
        super(name, resolveBooleanType(required), ENUMS, description, required);
    }

    private static Class<?> resolveBooleanType(boolean required) {
        if(required) {
            return Boolean.TYPE;
        } else {
            return Boolean.class;
        }
    }

}
