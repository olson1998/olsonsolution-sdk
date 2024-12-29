package com.olsonsolution.common.databind.domain.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.olsonsolution.common.databind.domain.port.repository.JacksonFormatterSpecManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.MODULE)
class MutableDateTimeJsonSerializer extends JsonSerializer<MutableDateTime> {

    private final DateTimeZone defaultTimeZone;

    private final DateTimeFormatter dateTimeFormatter;

    private final JacksonFormatterSpecManager jacksonFormatterSpecManager;

    @Override
    public void serialize(MutableDateTime mutableDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (mutableDateTime == null) {
            jsonGenerator.writeNull();
        } else {
            DateTimeZone timeZone = jacksonFormatterSpecManager.findThreadLocal()
                    .flatMap(spec -> Optional.ofNullable(spec.getWriteTimeZone()))
                    .orElse(defaultTimeZone);
            MutableDateTime timestamp = mutableDateTime.toMutableDateTime(timeZone);
            String stringDateTime = timestamp.toString(dateTimeFormatter);
            jsonGenerator.writeString(stringDateTime);
        }
    }
}
