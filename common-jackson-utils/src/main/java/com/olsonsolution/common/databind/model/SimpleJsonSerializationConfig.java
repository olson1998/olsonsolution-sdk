package com.olsonsolution.common.databind.model;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.olsonsolution.common.databind.domain.port.sterotype.JsonSerializationConfig;
import lombok.Data;
import lombok.NonNull;

@Data
public class SimpleJsonSerializationConfig<T> implements JsonSerializationConfig<T> {

    @NonNull
    private final Class<T> javaClass;

    private final JsonSerializer<T> stdSerializer;

    private final JsonDeserializer<T> stdDeserializer;

}
