package com.olsonsolution.common.databind.domain.service;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.olsonsolution.common.databind.domain.port.repository.DatabindModuleSupplier;
import com.olsonsolution.common.databind.domain.port.repository.ObjectMapperConfigurer;
import com.olsonsolution.common.databind.domain.port.sterotype.JsonSerializationConfig;
import com.olsonsolution.common.databind.domain.port.sterotype.TypeBind;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ObjectMapperConfigurationService implements ObjectMapperConfigurer {

    @Override
    public void configure(ObjectMapper objectMapper, List<? extends DatabindModuleSupplier> databindModuleSuppliers) {
        List<Module> modules = new ArrayList<>();
        SimpleModule mappingsModule = databindModuleSuppliers.stream()
                .flatMap(databindModuleSupplier -> databindModuleSupplier.getTypeBinds().stream())
                .collect(Collectors.collectingAndThen(Collectors.toMap(TypeBind::getAbstractClass, TypeBind::getJavaClass), this::collectToMappingModule));
        SimpleModule jsonSerializationModule = databindModuleSuppliers.stream()
                .flatMap(databindModuleSupplier -> databindModuleSupplier.getStdSerializationConfigs().stream())
                .collect(Collectors.collectingAndThen(Collectors.toList(), this::collectToJsonSerializationModule));
        modules.add(mappingsModule);
        modules.add(jsonSerializationModule);
        databindModuleSuppliers.forEach(databindModuleSupplier -> modules.addAll(databindModuleSupplier.getModules()));
        modules.forEach(objectMapper::registerModule);
    }

    private <A, T extends A> SimpleModule collectToMappingModule(Map<Class<A>, Class<T>> mappings) {
        SimpleModule mappingModule = new SimpleModule();
        mappings.forEach(mappingModule::addAbstractTypeMapping);
        return mappingModule;
    }

    private SimpleModule collectToJsonSerializationModule(List<JsonSerializationConfig<Object>> jsonSerializationConfigs) {
        SimpleModule jsonSerializationModule = new SimpleModule();
        jsonSerializationConfigs.forEach(config -> registerJsonSerialization(config, jsonSerializationModule));
        return jsonSerializationModule;
    }

    private void registerJsonSerialization(JsonSerializationConfig<Object> config, SimpleModule module) {
        Class<Object> javaClass = config.getJavaClass();
        Optional.ofNullable(config.getStdSerializer()).ifPresent(jsonWrite -> module.addSerializer(javaClass, jsonWrite));
        Optional.ofNullable(config.getStdDeserializer()).ifPresent(jsonRead -> module.addDeserializer(javaClass, jsonRead));
    }

}
