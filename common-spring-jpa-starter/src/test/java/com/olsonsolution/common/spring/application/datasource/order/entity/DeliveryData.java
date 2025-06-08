package com.olsonsolution.common.spring.application.datasource.order.entity;

import com.olsonsolution.common.spring.application.annotation.migration.ChangeSet;
import com.olsonsolution.common.spring.application.datasource.model.audit.AuditableEntity;
import com.olsonsolution.common.spring.application.datasource.order.entity.support.DeliveryStatus;
import com.olsonsolution.common.spring.application.datasource.order.entity.support.DeliveryType;
import com.olsonsolution.common.spring.application.jpa.service.AuditableEntityListener;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

@Entity
@ChangeSet
@Table(name = "delivery")
@EntityListeners({AuditableEntityListener.class})
public class DeliveryData extends AuditableEntity {

    @Id
    @Column(name = "id", length = 63, nullable = false, unique = true)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 31)
    private DeliveryType type;

    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    private DeliveryStatus deliveryStatus;

}
