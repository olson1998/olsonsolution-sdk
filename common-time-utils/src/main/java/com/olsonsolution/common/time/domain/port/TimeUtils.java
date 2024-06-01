package com.olsonsolution.common.time.domain.port;

import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;

public interface TimeUtils {

    DateTimeZone getTimeZone();

    MutableDateTime getTimestamp();

}
