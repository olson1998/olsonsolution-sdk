package com.olsonsolution.common.spring.application.hibernate;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Embeddable
public class EmbeddedTimestamp {

    @Column(name = "date_time", nullable = false)
    private Timestamp dateTime;

    @Column(name = "zone_id", length = 32)
    private String zoneId;

    public EmbeddedTimestamp(MutableDateTime dateTime) {
        this.dateTime = Timestamp.valueOf(LocalDateTime.of(
                dateTime.getYear(),
                dateTime.getMonthOfYear(),
                dateTime.getDayOfMonth(),
                dateTime.getHourOfDay(),
                dateTime.getMinuteOfHour(),
                dateTime.getSecondOfMinute(),
                dateTime.getMillisOfSecond() * 1000000
        ));
        this.zoneId = dateTime.getZone() != null ? dateTime.getZone().getID() : null;
    }

    public MutableDateTime toMutableDateTime() {
        LocalDateTime timestamp = dateTime.toLocalDateTime();
        return new MutableDateTime(
                timestamp.getYear(),
                timestamp.getMonthValue(),
                timestamp.getDayOfMonth(),
                timestamp.getHour(),
                timestamp.getMinute(),
                timestamp.getSecond(),
                timestamp.getNano() / 1000000,
                DateTimeZone.forID(zoneId)
        );
    }

}
