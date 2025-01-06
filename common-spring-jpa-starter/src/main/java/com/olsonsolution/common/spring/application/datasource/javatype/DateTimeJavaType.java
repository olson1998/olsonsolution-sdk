package com.olsonsolution.common.spring.application.datasource.javatype;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.BasicJavaType;
import org.joda.time.DateTime;

import java.time.LocalDateTime;

public class DateTimeJavaType implements BasicJavaType<DateTime> {

    @Override
    public <X> X unwrap(DateTime value, Class<X> type, WrapperOptions options) {
        if (value == null) {
            return null;
        } else if (LocalDateTime.class.isAssignableFrom(type)) {
            LocalDateTime localDateTime = LocalDateTime.of(
                    value.getYear(),
                    value.getMonthOfYear(),
                    value.getDayOfMonth(),
                    value.getHourOfDay(),
                    value.getMinuteOfHour(),
                    value.getSecondOfMinute(),
                    value.getMillisOfSecond()
            );
            return type.cast(localDateTime);
        } else {
            throw new IllegalArgumentException("Unsupported datetime type: " + type);
        }
    }

    @Override
    public <X> DateTime wrap(X value, WrapperOptions options) {
        if (value == null) {
            return null;
        } else if (value instanceof LocalDateTime localDateTime) {
            return new DateTime(
                    localDateTime.getYear(),
                    localDateTime.getMonthValue(),
                    localDateTime.getDayOfMonth(),
                    localDateTime.getHour(),
                    localDateTime.getMinute(),
                    localDateTime.getSecond(),
                    localDateTime.getNano()
            );
        } else {
            throw new IllegalArgumentException("Unsupported datetime type: " + value);
        }
    }
}

