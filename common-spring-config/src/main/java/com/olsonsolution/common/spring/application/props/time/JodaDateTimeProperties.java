package com.olsonsolution.common.spring.application.props.time;

import lombok.Data;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static com.olsonsolution.common.spring.application.props.time.JodaDateTimeProperties.JODA_DATE_TIME_PROPERTIES_BEAN;
import static com.olsonsolution.common.spring.application.props.time.JodaDateTimeProperties.JODA_DATE_TIME_PROPERTIES_PREFIX;
import static org.joda.time.DateTimeZone.UTC;

@Data
@Configuration(JODA_DATE_TIME_PROPERTIES_BEAN)
@ConfigurationProperties(JODA_DATE_TIME_PROPERTIES_PREFIX)
public class JodaDateTimeProperties {

    public static final String JODA_DATE_TIME_PROPERTIES_BEAN = "jodaDateTimeProperties";

    public static final String JODA_DATE_TIME_PROPERTIES_PREFIX = "spring.application.date-time";

    public static final String DEFAULT_TIME_ZONE_VALUE = "#{" + JODA_DATE_TIME_PROPERTIES_BEAN + ".timeZone}";

    public static final String DEFAULT_DATE_TIME_FORMAT = "#{" + JODA_DATE_TIME_PROPERTIES_BEAN + ".dateTimeFormat}";

    private DateTimeZone timeZone = UTC;

    private DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss.SSSZZ a");

}
