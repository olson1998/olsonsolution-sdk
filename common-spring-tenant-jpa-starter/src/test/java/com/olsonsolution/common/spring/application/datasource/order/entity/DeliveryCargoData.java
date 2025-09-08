package com.olsonsolution.common.spring.application.datasource.order.entity;

import com.olsonsolution.common.spring.application.annotation.migration.ChangeSet;
import com.olsonsolution.common.spring.application.datasource.order.entity.support.DeliveryCargo;
import com.olsonsolution.common.spring.application.jpa.service.AuditableEntityListener;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

@Entity
@ChangeSet
@Table(name = "delivery_cargo")
@EntityListeners({AuditableEntityListener.class})
public class DeliveryCargoData {

    @EmbeddedId
    private DeliveryCargo cargo;

}
