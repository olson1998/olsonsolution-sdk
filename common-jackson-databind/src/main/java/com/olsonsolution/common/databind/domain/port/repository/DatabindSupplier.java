package com.olsonsolution.common.databind.domain.port.repository;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

public interface DatabindSupplier {

    /**
     * Returns data bind module for joda-time library
     * @param dateTimeFormatter Date time formatting mutable date time to string
     * @return Joda date-time databind moudle
     */
    DatabindConfigurer configJodaDateTimeDataBindSupplier(DateTimeZone timeZone,
                                                          DateTimeFormatter dateTimeFormatter,
                                                          JacksonFormatterSpecManager specManager);

}
