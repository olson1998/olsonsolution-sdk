package com.olsonsolution.common.time.domain.service;

import com.olsonsolution.common.time.domain.port.TimeUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormatter;

@Getter
@RequiredArgsConstructor
public class TimeUtilityService implements TimeUtils {

    private final DateTimeZone timeZone;

    private final DateTimeFormatter dateTimeFormatter;

    @Override
    public MutableDateTime getTimestamp() {
        return MutableDateTime.now(timeZone);
    }
}
