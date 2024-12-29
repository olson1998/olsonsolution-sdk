package com.olsonsolution.common.databind.domain.service;

import com.olsonsolution.common.databind.domain.port.repository.DatabindConfigurer;
import com.olsonsolution.common.databind.domain.port.repository.DatabindSupplier;
import com.olsonsolution.common.databind.domain.port.repository.JacksonFormatterSpecManager;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormatter;

public class DatabindConfigurationService implements DatabindSupplier {

    @Override
    public DatabindConfigurer configJodaDateTimeDataBindSupplier(DateTimeZone timeZone,
                                                                 DateTimeFormatter dateTimeFormatter,
                                                                 JacksonFormatterSpecManager specManager) {
        DatabindConfigurer databindConfigurer = new DefaultDataBindConfigurer();
        MutableDateTimeJsonSerializer mutableDateTimeJsonSerializer = new MutableDateTimeJsonSerializer(
                timeZone,
                dateTimeFormatter,
                specManager
        );
        MutableDateTimeJsonDeserializer mutableDateTimeJsonDeserializer = new MutableDateTimeJsonDeserializer(dateTimeFormatter);
        databindConfigurer.registerSerializationConfig(
                MutableDateTime.class,
                mutableDateTimeJsonSerializer,
                mutableDateTimeJsonDeserializer
        );
        return databindConfigurer;
    }
}
