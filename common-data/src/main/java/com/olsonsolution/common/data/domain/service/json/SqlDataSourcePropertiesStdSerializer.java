package com.olsonsolution.common.data.domain.service.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Properties;

public class SqlDataSourcePropertiesStdSerializer extends StdSerializer<Properties> {

    public SqlDataSourcePropertiesStdSerializer() {
        super(Properties.class);
    }

    @Override
    public void serialize(Properties properties, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (properties == null) {
            jsonGenerator.writeNull();
        } else {
            jsonGenerator.writeStartObject();
            for (String property : properties.stringPropertyNames()) {
                jsonGenerator.writeStringField(property, properties.getProperty(property));
            }
            jsonGenerator.writeEndObject();
        }
    }
}
