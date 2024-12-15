package com.olsonsolution.common.data.domain.service.json;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class SqlDataSourcePropertiesStdDeserializer extends StdDeserializer<Properties> {

    public SqlDataSourcePropertiesStdDeserializer() {
        super(Properties.class);
    }

    @Override
    public Properties deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        ObjectCodec objectCodec = jsonParser.getCodec();
        JsonNode json = objectCodec.readTree(jsonParser);
        if(json instanceof NullNode) {
            return null;
        } else if (json instanceof ObjectNode jsonObject) {
            Properties properties = new Properties();
            for(Map.Entry<String, JsonNode> propertiesJson : jsonObject.properties()) {
                properties.put(propertiesJson.getKey(), propertiesJson.getValue().asText());
            }
            return properties;
        } else {
            throw new JsonParseException("Expected JSON object");
        }
    }
}
