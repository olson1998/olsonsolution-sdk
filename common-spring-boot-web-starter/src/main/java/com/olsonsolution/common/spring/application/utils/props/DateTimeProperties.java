package com.olsonsolution.common.spring.application.utils.props;

import jakarta.annotation.Nonnull;
import lombok.Data;
import org.joda.time.DateTimeZone;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "olsonsolution.common.spring.application.utils.time")
public class DateTimeProperties {

    @Nonnull
    private DateTimeZone timeZone;

}
