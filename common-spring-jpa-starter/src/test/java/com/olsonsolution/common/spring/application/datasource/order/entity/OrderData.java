package com.olsonsolution.common.spring.application.datasource.order.entity;

import com.olsonsolution.common.spring.application.annotation.migration.ChangeSet;
import com.olsonsolution.common.spring.application.annotation.migration.ForeignKey;
import com.olsonsolution.common.spring.application.datasource.model.audit.AuditableEntity;
import com.olsonsolution.common.spring.application.hibernate.EmbeddedTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

@Entity
@ChangeSet
@Table(name = "order")
public class OrderData extends AuditableEntity {

    @Id
    @Column(name = "id", length = 63, nullable = false, unique = true)
    private String id;

    @Column(name = "from", nullable = false, length = 63)
    private String fromClient;

    @Column(name = "item_id", length = 63, nullable = false)
    @ForeignKey(referenceJpaSpec = "WarehouseIndex", referenceTable = "item", referenceColumn = "id", version = "1.0.0")
    private String itemId;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    private EmbeddedTimestamp until;

}
