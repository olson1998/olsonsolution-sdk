package com.olsonsolution.common.databind.domain.port.stereotype;

import org.joda.time.DateTimeZone;

public interface JacksonFormatterSpec {

    DateTimeZone getWriteTimeZone();

}
