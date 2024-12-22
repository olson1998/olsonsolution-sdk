package com.olsonsolution.common.property.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EnumPropertySpec extends PropertySpecModel {

    @Builder(builderMethodName = "enumPropertySpec")
    public EnumPropertySpec(@NonNull String name,
                            @NonNull Class<? extends Enum> type,
                            String description,
                            boolean required) {
        super(name, type, resolveEnumValues(type), description, required);
    }

    private static Set<String> resolveEnumValues(Class<? extends Enum> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());
    }

}
