package com.olsonsolution.common.spring.application.datasource.javatype;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.BasicJavaType;
import org.joda.time.DateTimeZone;

public class DateTimeZoneJavaType implements BasicJavaType<DateTimeZone> {

    @Override
    public <X> X unwrap(DateTimeZone value, Class<X> type, WrapperOptions options) {
        if (value == null) {
            return null;
        } else if (String.class.equals(type)) {
            return type.cast(value.getID());
        } else {
            throw new IllegalArgumentException("Unsupported datetime zone type: " + value);
        }
    }

    @Override
    public <X> DateTimeZone wrap(X value, WrapperOptions options) {
        if (value == null) {
            return null;
        } else if (value instanceof String id) {
            return DateTimeZone.forID(id);
        } else {
            throw new IllegalArgumentException("Unsupported datetime zone type: " + value);
        }
    }

}
