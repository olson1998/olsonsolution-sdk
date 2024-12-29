package com.olsonsolution.common.databind.domain.model;

import com.olsonsolution.common.databind.domain.port.stereotype.JacksonFormatterSpec;
import lombok.Builder;
import lombok.Data;
import org.joda.time.DateTimeZone;

@Data
@Builder
public class JacksonFormatterSpecification implements JacksonFormatterSpec {

    private final DateTimeZone writeTimeZone;

}
