package com.olsonsolution.common.time.domain.service;

import com.olsonsolution.common.time.domain.port.TimeUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;

@RequiredArgsConstructor
public class TimeUtilityService implements TimeUtils {

    @Getter
    private final DateTimeZone timeZone;

    @Override
    public MutableDateTime getTimestamp() {
        return MutableDateTime.now(timeZone);
    }
}
