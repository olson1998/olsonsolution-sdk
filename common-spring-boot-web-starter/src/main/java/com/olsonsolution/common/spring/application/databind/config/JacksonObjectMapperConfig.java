package com.olsonsolution.common.spring.application.databind.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olsonsolution.common.databind.domain.port.repository.DatabindConfigurer;
import com.olsonsolution.common.databind.domain.port.repository.DatabindSupplier;
import com.olsonsolution.common.databind.domain.port.repository.ObjectMapperConfigurer;
import com.olsonsolution.common.databind.domain.service.DatabindConfigurationService;
import com.olsonsolution.common.databind.domain.service.ObjectMapperConfigurationService;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.olsonsolution.common.spring.application.props.time.JodaDateTimeProperties.DEFAULT_DATE_TIME_FORMAT;
import static com.olsonsolution.common.spring.application.props.time.JodaDateTimeProperties.DEFAULT_TIME_ZONE_VALUE;

@Configuration
public class JacksonObjectMapperConfig {

    private final DatabindSupplier databindSupplier = new DatabindConfigurationService();

    @Bean
    public DatabindConfigurer jodaDataTimeDataBindConfigurer(
            @Value(DEFAULT_TIME_ZONE_VALUE) DateTimeZone timeZone,
            @Value(DEFAULT_DATE_TIME_FORMAT) DateTimeFormatter dateTimeFormatter) {
        return databindSupplier.configJodaDateTimeDataBindSupplier(
                timeZone,
                dateTimeFormatter
        );
    }

    @Bean
    public ObjectMapper objectMapper(List<DatabindConfigurer> databindConfigurerList) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectMapperConfigurer objectMapperConfigurer = new ObjectMapperConfigurationService();
        objectMapperConfigurer.configure(objectMapper, databindConfigurerList);
        return objectMapper;
    }

}
