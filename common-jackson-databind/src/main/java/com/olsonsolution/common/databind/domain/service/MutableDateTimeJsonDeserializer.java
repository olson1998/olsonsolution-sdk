package com.olsonsolution.common.databind.domain.service;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

@RequiredArgsConstructor(access = AccessLevel.MODULE)
class MutableDateTimeJsonDeserializer extends JsonDeserializer<MutableDateTime> {

    private final DateTimeFormatter dateTimeFormatter;

    @Override
    public MutableDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        ObjectCodec objectCodec = jsonParser.getCodec();
        JsonNode json = objectCodec.readValue(jsonParser, JsonNode.class);
        if(json instanceof TextNode string) {
            return dateTimeFormatter.parseMutableDateTime(string.textValue());
        } else if (json instanceof NullNode nullValue) {
            return null;
        } else {
            throw new IOException("%s expected %s or %s got %s".formatted(
                    MutableDateTime.class.getCanonicalName(),
                    TextNode.class.getCanonicalName(),
                    NullNode.class.getCanonicalName(),
                    json.getClass().getCanonicalName()
            ));
        }
    }
}
