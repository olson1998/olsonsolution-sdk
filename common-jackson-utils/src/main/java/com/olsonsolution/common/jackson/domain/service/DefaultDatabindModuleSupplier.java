package com.olsonsolution.common.jackson.domain.service;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.olsonsolution.common.jackson.domain.port.repository.DatabindModuleSupplier;
import com.olsonsolution.common.jackson.domain.port.sterotype.JsonSerializationConfig;
import com.olsonsolution.common.jackson.domain.port.sterotype.TypeBind;
import com.olsonsolution.common.jackson.model.SimpleJsonSerializationConfig;
import com.olsonsolution.common.jackson.model.SimpleTypeBind;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;

@Getter
public class DefaultDatabindModuleSupplier implements DatabindModuleSupplier {

    private final Collection<Module> modules = new ArrayList<>();

    private final Collection<TypeBind<Object, Object>> typeBinds = new ArrayList<>();

    private final Collection<JsonSerializationConfig<Object>> jsonSerializationConfigs = new ArrayList<>();

    @Override
    public void registerModule(Module module) {
        modules.add(module);
    }

    @Override
    public <T> void registerSerializationConfig(Class<T> javaClass, Class<? extends StdSerializer<T>> stdSerializer, Class<? extends StdDeserializer<T>> stdDeserializer) {
        JsonSerializationConfig<T> jsonSerializationConfig = new SimpleJsonSerializationConfig<>(
                javaClass,
                stdSerializer,
                stdDeserializer
        );
        jsonSerializationConfigs.add((JsonSerializationConfig<Object>) jsonSerializationConfig);
    }

    @Override
    public <T, S extends T> void registerAbstractTypeBind(Class<T> abstractClass, Class<S> javaClass) {
        TypeBind<T, ?> typeBind = new SimpleTypeBind<>(abstractClass, javaClass);
        typeBinds.add((TypeBind<Object, Object>) typeBind);
    }
}
