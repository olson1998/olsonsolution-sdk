package com.olsonsolution.common.databind.domain.port.repository;

import org.joda.time.format.DateTimeFormatter;

public interface DataBindConfigurer {

    /**
     * Returns data bind module for joda-time library
     * @param dateTimeFormatter Date time formatting mutable date time to string
     * @return Joda date-time databind moudle
     */
    DatabindConfigurer configJodaDateTimeDataBindSupplier(DateTimeFormatter dateTimeFormatter);

}
