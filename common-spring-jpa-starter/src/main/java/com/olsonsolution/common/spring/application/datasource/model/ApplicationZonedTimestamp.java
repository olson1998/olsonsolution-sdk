package com.olsonsolution.common.spring.application.datasource.model;

import com.olsonsolution.common.spring.application.datasource.javatype.DateTimeJavaType;
import com.olsonsolution.common.spring.application.datasource.javatype.DateTimeZoneJavaType;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.ZonedTimestamp;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JavaType;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.TimestampJdbcType;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationZonedTimestamp implements ZonedTimestamp {

    @JdbcType(TimestampJdbcType.class)
    @JavaType(DateTimeJavaType.class)
    private DateTime dateTime;

    @JdbcType(VarcharJdbcType.class)
    @JavaType(DateTimeZoneJavaType.class)
    private DateTimeZone dateTimeZone;

    @Override
    public MutableDateTime toTimestamp() {
        return new MutableDateTime(dateTime, dateTimeZone);
    }
}
