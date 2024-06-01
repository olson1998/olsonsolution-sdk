package com.olsonsolution.common.databind.domain.service;

import com.olsonsolution.common.databind.domain.port.repository.DataBindConfigurer;
import com.olsonsolution.common.databind.domain.port.repository.DatabindConfigurer;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormatter;

public class DataBindConfigurationService implements DataBindConfigurer {

    @Override
    public DatabindConfigurer configJodaDateTimeDataBindSupplier(DateTimeFormatter dateTimeFormatter) {
        DatabindConfigurer databindConfigurer = new DefaultDataBindConfigurer();
        MutableDateTimeJsonSerializer mutableDateTimeJsonSerializer = new MutableDateTimeJsonSerializer(dateTimeFormatter);
        MutableDateTimeJsonDeserializer mutableDateTimeJsonDeserializer = new MutableDateTimeJsonDeserializer(dateTimeFormatter);
        databindConfigurer.registerSerializationConfig(
                MutableDateTime.class,
                mutableDateTimeJsonSerializer,
                mutableDateTimeJsonDeserializer
        );
        return databindConfigurer;
    }
}
