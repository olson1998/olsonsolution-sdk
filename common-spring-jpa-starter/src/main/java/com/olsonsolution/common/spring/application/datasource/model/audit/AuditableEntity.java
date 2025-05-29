package com.olsonsolution.common.spring.application.datasource.model.audit;

import com.olsonsolution.common.spring.application.hibernate.EmbeddedTimestamp;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString

@NoArgsConstructor
@AllArgsConstructor

@MappedSuperclass
@AttributeOverrides({
        @AttributeOverride(name = "creation.dateTime", column = @Column(name = "creation_timestamp", nullable = false)),
        @AttributeOverride(name = "creation.zoneId", column = @Column(name = "creation_timestamp_timezone", nullable = false, length = 32)),
        @AttributeOverride(name = "lastUpdate.dateTime", column = @Column(name = "last_update_timestamp", nullable = false)),
        @AttributeOverride(name = "lastUpdate.zoneId", column = @Column(name = "last_update_timestamp_timezone", nullable = false, length = 32))
})
public class AuditableEntity {

    @Embedded
    private EmbeddedTimestamp creation;

    @Embedded
    private EmbeddedTimestamp lastUpdate;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

}
