package com.olsonsolution.common.databind.domain.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

@RequiredArgsConstructor(access = AccessLevel.MODULE)
class MutableDateTimeJsonSerializer extends JsonSerializer<MutableDateTime> {

    private final DateTimeFormatter dateTimeFormatter;

    @Override
    public void serialize(MutableDateTime mutableDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if(mutableDateTime == null) {
            jsonGenerator.writeNull();
        } else {
            String stringDateTime = mutableDateTime.toString(dateTimeFormatter);
            jsonGenerator.writeString(stringDateTime);
        }
    }
}
