package com.olsonsolution.common.jackson.domain.port.repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public interface ObjectMapperConfigurer {

    void configure(ObjectMapper objectMapper, List<? extends DatabindModuleSupplier> databindModuleSuppliers);

}
