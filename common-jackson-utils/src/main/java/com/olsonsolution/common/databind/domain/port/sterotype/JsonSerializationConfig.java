package com.olsonsolution.common.databind.domain.port.sterotype;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;

public interface JsonSerializationConfig<T> {

    Class<T> getJavaClass();

    JsonSerializer<T> getStdSerializer();

    JsonDeserializer<T> getStdDeserializer();

}
