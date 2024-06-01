package com.olsonsolution.common.databind.domain.service;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.olsonsolution.common.databind.domain.model.SimpleJsonSerializationConfig;
import com.olsonsolution.common.databind.domain.port.repository.DatabindConfigurer;
import com.olsonsolution.common.databind.domain.port.sterotype.JsonSerializationConfig;
import com.olsonsolution.common.databind.domain.port.sterotype.TypeBind;
import com.olsonsolution.common.databind.domain.model.SimpleTypeBind;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;

@Getter
public class DefaultDataBindConfigurer implements DatabindConfigurer {

    private final Collection<Module> modules = new ArrayList<>();

    private final Collection<TypeBind<Object, Object>> typeBinds = new ArrayList<>();

    private final Collection<JsonSerializationConfig<Object>> jsonSerializationConfigs = new ArrayList<>();

    @Override
    public void registerModule(Module module) {
        modules.add(module);
    }

    @Override
    public <T> void registerSerializationConfig(Class<T> javaClass, JsonSerializer<T> jsonSerializer, JsonDeserializer<T> jsonDeserializer) {
        JsonSerializationConfig<T> jsonSerializationConfig = new SimpleJsonSerializationConfig<>(
                javaClass,
                jsonSerializer,
                jsonDeserializer
        );
        jsonSerializationConfigs.add((JsonSerializationConfig<Object>) jsonSerializationConfig);
    }

    @Override
    public <T, S extends T> void registerAbstractTypeBind(Class<T> abstractClass, Class<S> javaClass) {
        TypeBind<T, ?> typeBind = new SimpleTypeBind<>(abstractClass, javaClass);
        typeBinds.add((TypeBind<Object, Object>) typeBind);
    }
}
