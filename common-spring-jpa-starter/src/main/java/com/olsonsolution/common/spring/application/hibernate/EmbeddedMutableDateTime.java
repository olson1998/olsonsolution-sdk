package com.olsonsolution.common.spring.application.hibernate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.MutableDateTime;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddedMutableDateTime {

    private LocalDateTime dateTime;

    private String zoneId;

    public MutableDateTime toDateTime() {
        return new MutableDateTime(
                dateTime.getYear(),
                dateTime.getMonthValue(),
                dateTime.getDayOfMonth(),
                dateTime.getHour(),
                dateTime.getMinute(),
                dateTime.getSecond(),
                dateTime.getNano() / 1000000
        );
    }

}
