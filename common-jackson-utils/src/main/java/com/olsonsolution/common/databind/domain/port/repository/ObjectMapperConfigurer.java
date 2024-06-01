package com.olsonsolution.common.databind.domain.port.repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public interface ObjectMapperConfigurer {

    void configure(ObjectMapper objectMapper, List<? extends DatabindConfigurer> databindModuleSuppliers);

}
