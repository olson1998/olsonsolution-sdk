package com.olsonsolution.common.spring.domain.service.time;

import org.joda.time.DateTimeZone;
import org.springframework.core.convert.converter.Converter;

public class DateTimeZoneConvertingService implements Converter<String, DateTimeZone> {

    @Override
    public DateTimeZone convert(String source) {
        return DateTimeZone.forID(source);
    }
}
