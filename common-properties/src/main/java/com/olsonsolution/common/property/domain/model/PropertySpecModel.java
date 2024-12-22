package com.olsonsolution.common.property.domain.model;

import com.olsonsolution.common.property.domain.port.stereotype.PropertySpec;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

@Data
@Builder(builderMethodName = "propertySpec")
@NoArgsConstructor
@AllArgsConstructor
public class PropertySpecModel implements PropertySpec {

    @NonNull
    private String name;

    @NonNull
    @Builder.Default
    private Class<?> type = String.class;

    private Set<String> enums;

    @Builder.Default
    private String description = StringUtils.EMPTY;

    @Builder.Default
    private boolean required = false;

}
