package com.olsonsolution.common.spring.application.datasource.model.audit;

import com.olsonsolution.common.spring.application.hibernate.MutableDataTimeJavaType;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.*;
import org.hibernate.annotations.JavaType;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.OffsetDateTimeJdbcType;
import org.joda.time.MutableDateTime;

@Getter
@Setter
@ToString

@NoArgsConstructor
@AllArgsConstructor

@MappedSuperclass
public class AuditableEntity {

    @JavaType(MutableDataTimeJavaType.class)
    @JdbcType(OffsetDateTimeJdbcType.class)
    @Column(name = "creation_timestamp", nullable = false)
    private MutableDateTime creationTimestamp;

    @JavaType(MutableDataTimeJavaType.class)
    @JdbcType(OffsetDateTimeJdbcType.class)
    @Column(name = "last_update_timestamp", nullable = false)
    private MutableDateTime lastUpdateTimestamp;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

}
