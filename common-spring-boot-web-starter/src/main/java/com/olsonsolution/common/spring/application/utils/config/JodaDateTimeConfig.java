package com.olsonsolution.common.spring.application.utils.config;

import com.olsonsolution.common.spring.application.utils.props.DateTimeProperties;
import com.olsonsolution.common.time.domain.port.TimeUtils;
import com.olsonsolution.common.time.domain.service.TimeUtilityService;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JodaDateTimeConfig {

    @Bean
    public DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss.SSSZZ a");
    }

    @Bean
    public TimeUtils timeUtils(DateTimeProperties dateTimeProperties,
                               DateTimeFormatter dateTimeFormatter) {
        return new TimeUtilityService(
                dateTimeProperties.getTimeZone(),
                dateTimeFormatter
        );
    }

}
