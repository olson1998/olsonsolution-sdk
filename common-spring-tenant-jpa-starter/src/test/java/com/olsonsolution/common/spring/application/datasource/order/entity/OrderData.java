package com.olsonsolution.common.spring.application.datasource.order.entity;

import com.olsonsolution.common.spring.application.annotation.migration.ChangeSet;
import com.olsonsolution.common.spring.application.annotation.migration.ForeignKey;
import com.olsonsolution.common.spring.application.datasource.model.audit.AuditableEntity;
import com.olsonsolution.common.spring.application.hibernate.EmbeddedTimestamp;
import com.olsonsolution.common.spring.application.jpa.service.AuditableEntityListener;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor

@Entity
@ChangeSet
@Table(name = "item_order")
@EntityListeners({AuditableEntityListener.class})
public class OrderData extends AuditableEntity {

    @Id
    @Column(name = "id", length = 63, nullable = false, unique = true)
    private String id;

    @Column(name = "from_client", nullable = false, length = 63)
    private String fromClient;

    @Column(name = "item_id", length = 63, nullable = false)
    @ForeignKey(referenceJpaSpec = "WarehouseIndex", referenceTable = "item", referenceColumn = "id", version = "1.0.0")
    private String itemId;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @AttributeOverrides({
            @AttributeOverride(name = "dateTime", column = @Column(name = "complete_until_timestamp", nullable = false)),
            @AttributeOverride(name = "zoneId", column = @Column(name = "complete_until_timestamp_timezone", nullable = false, length = 32)),
    })
    private EmbeddedTimestamp completeUntil;

    @AttributeOverrides({
            @AttributeOverride(name = "dateTime", column = @Column(name = "deliver_until_timestamp", nullable = false)),
            @AttributeOverride(name = "zoneId", column = @Column(name = "deliver_until_timestamp_timezone", nullable = false, length = 32)),
    })
    private EmbeddedTimestamp deliverUntil;

    @Builder(builderMethodName = "newOrder", builderClassName = "NewOrderBuilder")
    public OrderData(String id, EmbeddedTimestamp deliverUntil, EmbeddedTimestamp completeUntil,
                     Long quantity, String itemId, String fromClient) {
        this.id = id;
        this.deliverUntil = deliverUntil;
        this.completeUntil = completeUntil;
        this.quantity = quantity;
        this.itemId = itemId;
        this.fromClient = fromClient;
    }

    public OrderData(EmbeddedTimestamp creation, EmbeddedTimestamp lastUpdate, Long version, String id,
                     EmbeddedTimestamp deliverUntil, EmbeddedTimestamp completeUntil,
                     Long quantity, String itemId, String fromClient) {
        super(creation, lastUpdate, version);
        this.id = id;
        this.deliverUntil = deliverUntil;
        this.completeUntil = completeUntil;
        this.quantity = quantity;
        this.itemId = itemId;
        this.fromClient = fromClient;
    }
}
