package com.olsonsolution.common.spring.application.hibernate;

import jakarta.persistence.TemporalType;
import lombok.Getter;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTemporalJavaType;
import org.hibernate.type.descriptor.java.TemporalJavaType;
import org.hibernate.type.spi.TypeConfiguration;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.Timestamp;
import java.time.*;

import static java.time.ZoneOffset.UTC;

public class MutableDataTimeJavaType extends AbstractTemporalJavaType<MutableDateTime> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss.SSSZZ a");

    @Getter
    private final boolean temporalType = true;

    @Getter
    private final TemporalType precision = TemporalType.TIMESTAMP;

    public MutableDataTimeJavaType() {
        super(MutableDateTime.class);
    }

    @Override
    public <X> X unwrap(MutableDateTime value, Class<X> type, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        return type.cast(unwrapTimestamp(value, type));
    }

    @Override
    public <X> MutableDateTime wrap(X value, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        return wrapTimestamp(value);
    }

    @Override
    protected <X> TemporalJavaType<X> forTimestampPrecision(TypeConfiguration typeConfiguration) {
        return (TemporalJavaType<X>) this;
    }

    @Override
    protected <X> TemporalJavaType<X> forDatePrecision(TypeConfiguration typeConfiguration) {
        return (TemporalJavaType<X>) this;
    }

    @Override
    protected <X> TemporalJavaType<X> forTimePrecision(TypeConfiguration typeConfiguration) {
        return (TemporalJavaType<X>) this;
    }

    private Object unwrapTimestamp(MutableDateTime dateTime, Class<?> temporalType) {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(
                dateTime.getYear(),
                dateTime.getMonthOfYear(),
                dateTime.getDayOfMonth(),
                dateTime.getHourOfDay(),
                dateTime.getMinuteOfHour(),
                dateTime.getSecondOfMinute(),
                dateTime.getMillisOfSecond() * 1000000,
                ZoneId.of(dateTime.getZone().getID())
        );
        if (ZonedDateTime.class.isAssignableFrom(temporalType)) {
            return zonedDateTime;
        } else if (OffsetDateTime.class.isAssignableFrom(temporalType)) {
            return zonedDateTime.toOffsetDateTime();
        } else if (Timestamp.class.isAssignableFrom(temporalType)) {
            return new Timestamp(zonedDateTime.toInstant().toEpochMilli());
        } else if (LocalDateTime.class.isAssignableFrom(temporalType)) {
            return zonedDateTime.toLocalDateTime();
        } else if (Instant.class.isAssignableFrom(temporalType)) {
            return zonedDateTime.toInstant();
        } else if (Long.class.isAssignableFrom(temporalType)) {
            return zonedDateTime.toInstant().toEpochMilli();
        } else if (String.class.isAssignableFrom(temporalType)) {
            return dateTime.toString(DATE_TIME_FORMATTER);
        } else if (LocalDate.class.isAssignableFrom(temporalType)) {
            return LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
        } else if (LocalTime.class.isAssignableFrom(temporalType)) {
            return LocalTime.of(dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), dateTime.getSecondOfMinute());
        } else {
            throw unknownUnwrap(temporalType);
        }
    }

    private MutableDateTime wrapTimestamp(Object value) {
        int year;
        int month;
        int day;
        int hour;
        int minute;
        int second;
        int millisecond;
        DateTimeZone dateTimeZone;
        if (value instanceof ZonedDateTime dateTime) {
            year = dateTime.getYear();
            month = dateTime.getMonthValue();
            day = dateTime.getDayOfMonth();
            hour = dateTime.getHour();
            minute = dateTime.getMinute();
            second = dateTime.getSecond();
            millisecond = dateTime.getNano() / 1000000;
            dateTimeZone = DateTimeZone.forID(dateTime.getZone().getId());
        } else if (value instanceof OffsetDateTime dateTime) {
            return wrapTimestamp(dateTime.toZonedDateTime());
        } else if (value instanceof LocalDateTime dateTime) {
            return wrapTimestamp(dateTime.atZone(ZoneId.systemDefault()));
        } else if (value instanceof Instant dateTime) {
            return wrapTimestamp(ZonedDateTime.ofInstant(dateTime, UTC));
        } else if (value instanceof Timestamp dateTime) {
            return wrapTimestamp(ZonedDateTime.ofInstant(dateTime.toInstant(), UTC));
        } else if (value instanceof Long epochMillis) {
            return wrapTimestamp(Instant.ofEpochMilli(epochMillis));
        } else if (value instanceof String timestamp) {
            return DATE_TIME_FORMATTER.parseMutableDateTime(timestamp);
        } else if (value instanceof LocalDate date) {
            year = date.getYear();
            month = date.getMonthValue();
            day = date.getDayOfMonth();
            hour = 0;
            minute = 0;
            second = 0;
            millisecond = 0;
            dateTimeZone = DateTimeZone.UTC;
        } else if (value instanceof LocalTime time) {
            year = 0;
            month = 0;
            day = 0;
            hour = time.getHour();
            minute = time.getMinute();
            second = time.getSecond();
            millisecond = time.getNano() / 1000000;
            dateTimeZone = DateTimeZone.UTC;
        } else {
            throw unknownWrap(value.getClass());
        }
        return new MutableDateTime(year, month, day, hour, minute, second, millisecond, dateTimeZone);
    }


}
