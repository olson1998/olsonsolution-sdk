package com.olsonsolution.common.spring.domain.port.stereotype.datasource;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;

public interface ZonedTimestamp {

    DateTime getDateTime();

    DateTimeZone getDateTimeZone();

    MutableDateTime toTimestamp();

}
