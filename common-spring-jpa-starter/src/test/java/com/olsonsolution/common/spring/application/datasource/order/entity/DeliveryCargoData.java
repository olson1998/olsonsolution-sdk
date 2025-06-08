package com.olsonsolution.common.spring.application.datasource.order.entity;

import com.olsonsolution.common.spring.application.annotation.migration.ChangeSet;
import com.olsonsolution.common.spring.application.datasource.order.entity.support.DeliveryCargo;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
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
public class DeliveryCargoData {

    @EmbeddedId
    private DeliveryCargo cargo;

}
