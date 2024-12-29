package com.olsonsolution.common.spring.domain.service.time;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.core.convert.converter.Converter;

public class DateTimeZoneFormatterConversionService implements Converter<String, DateTimeFormatter> {

    @Override
    public DateTimeFormatter convert(String source) {
        return DateTimeFormat.forPattern(source);
    }
}
