package com.olsonsolution.common.jackson.domain.port.repository;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.olsonsolution.common.jackson.domain.port.sterotype.JsonSerializationConfig;
import com.olsonsolution.common.jackson.domain.port.sterotype.TypeBind;

import java.util.Collection;

public interface DatabindModuleSupplier {

    Collection<Module> getModules();

    Collection<TypeBind<Object, Object>> getTypeBinds();

    Collection<JsonSerializationConfig<Object>> getStdSerializationConfigs();

    void registerModule(Module module);

    <T> void registerSerializationConfig(Class<T> javaClass, Class<? extends StdSerializer<T>> stdSerializer, Class<? extends StdDeserializer<T>> stdDeserializer);

    <T, S extends T> void registerAbstractTypeBind(Class<T> abstractClass, Class<S> javaClass);

}
