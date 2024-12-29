package com.olsonsolution.common.spring.application.config.time;

import com.olsonsolution.common.spring.application.props.time.JodaDateTimeProperties;
import com.olsonsolution.common.spring.domain.service.time.DateTimeZoneConvertingService;
import com.olsonsolution.common.spring.domain.service.time.DateTimeZoneFormatterConversionService;
import com.olsonsolution.common.time.domain.port.TimeUtils;
import com.olsonsolution.common.time.domain.service.TimeUtilityService;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class TimeUtilsConfig {

    @Bean
    @ConfigurationPropertiesBinding
    public Converter<String, DateTimeZone> dateTimeZoneConverter() {
        return new DateTimeZoneConvertingService();
    }

    @Bean
    @ConfigurationPropertiesBinding
    public Converter<String, DateTimeFormatter> dateTimeFormatterConverter() {
        return new DateTimeZoneFormatterConversionService();
    }

    @Bean
    public TimeUtils timeUtils(JodaDateTimeProperties jodaDateTimeProperties) {
        return new TimeUtilityService(
                jodaDateTimeProperties.getTimeZone(),
                jodaDateTimeProperties.getDateTimeFormat()
        );
    }

}
