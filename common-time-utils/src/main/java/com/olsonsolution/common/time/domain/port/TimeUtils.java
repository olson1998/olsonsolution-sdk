package com.olsonsolution.common.time.domain.port;

import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormatter;

public interface TimeUtils {

    DateTimeZone getTimeZone();

    DateTimeFormatter getDateTimeFormatter();

    MutableDateTime getTimestamp();

}
