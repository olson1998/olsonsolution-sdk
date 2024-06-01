package com.olsonsolution.common.databind.domain.service;

import com.olsonsolution.common.databind.domain.port.DataBindConfigurer;
import com.olsonsolution.common.databind.domain.port.repository.DatabindModuleSupplier;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormatter;

public class DataBindConfigurationService implements DataBindConfigurer {

    @Override
    public DatabindModuleSupplier configJodaDateTimeDataBindSupplier(DateTimeFormatter dateTimeFormatter) {
        DatabindModuleSupplier databindModuleSupplier = new DefaultDatabindModuleSupplier();
        MutableDateTimeJsonSerializer mutableDateTimeJsonSerializer = new MutableDateTimeJsonSerializer(dateTimeFormatter);
        MutableDateTimeJsonDeserializer mutableDateTimeJsonDeserializer = new MutableDateTimeJsonDeserializer(dateTimeFormatter);
        databindModuleSupplier.registerSerializationConfig(
                MutableDateTime.class,
                mutableDateTimeJsonSerializer,
                mutableDateTimeJsonDeserializer
        );
        return databindModuleSupplier;
    }
}
