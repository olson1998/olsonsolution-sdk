package com.olsonsolution.common.spring.application.datasource.model.audit;

import com.olsonsolution.common.spring.application.hibernate.EmbeddedMutableDateTime;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString

@NoArgsConstructor
@AllArgsConstructor

@MappedSuperclass
@AttributeOverrides({
        @AttributeOverride(name = "creationTimestamp.dateTime", column = @Column(name = "creation_timestamp") ),
        @AttributeOverride(name = "creationTimestamp.zoneId", column = @Column(name = "creation_timestamp_timezone") ),
        @AttributeOverride(name = "lastUpdateTimestamp.dateTime", column = @Column(name = "last_update_timestamp")),
        @AttributeOverride(name = "lastUpdateTimestamp.zoneId", column = @Column(name = "last_update_timestamp_timezone"))
})
public class AuditableEntity {

    @Embedded
    private EmbeddedMutableDateTime creationTimestamp;

    @Embedded
    private EmbeddedMutableDateTime lastUpdateTimestamp;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

}
