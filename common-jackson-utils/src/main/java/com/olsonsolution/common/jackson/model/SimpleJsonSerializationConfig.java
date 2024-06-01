package com.olsonsolution.common.jackson.model;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.olsonsolution.common.jackson.domain.port.sterotype.JsonSerializationConfig;
import lombok.Data;
import lombok.NonNull;

@Data
public class SimpleJsonSerializationConfig<T> implements JsonSerializationConfig<T> {

    @NonNull
    private final Class<T> javaClass;

    private final JsonSerializer<T> stdSerializer;

    private final JsonDeserializer<T> stdDeserializer;

}
