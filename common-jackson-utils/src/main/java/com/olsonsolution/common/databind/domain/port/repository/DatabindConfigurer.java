package com.olsonsolution.common.databind.domain.port.repository;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.olsonsolution.common.databind.domain.port.sterotype.JsonSerializationConfig;
import com.olsonsolution.common.databind.domain.port.sterotype.TypeBind;

import java.util.Collection;

public interface DatabindConfigurer {

    /**
     * Returns collection of pre-config modules
     * @return Collection of pre-config modules
     */
    Collection<Module> getModules();

    /**
     * Returns collection of binds between abstract java class and java class
     * @return Collection of binds between abstract java class and java class
     */
    Collection<TypeBind<Object, Object>> getTypeBinds();

    Collection<JsonSerializationConfig<Object>> getJsonSerializationConfigs();

    void registerModule(Module module);

    <T> void registerSerializationConfig(Class<T> javaClass, JsonSerializer<T> jsonSerializer, JsonDeserializer<T> jsonDeserializer);

    <T, S extends T> void registerAbstractTypeBind(Class<T> abstractClass, Class<S> javaClass);

}
