package com.olsonsolution.common.spring.application.datasource.order.entity.support;

import com.olsonsolution.common.spring.application.annotation.migration.ForeignKey;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Embeddable
public class DeliveryCargo {

    @Column(name = "delivery_id", nullable = false, length = 63)
    @ForeignKey(referenceTable = "delivery", referenceColumn = "id", version = "1.0.0")
    private String deliveryId;

    @Column(name = "order_id", nullable = false, length = 63)
    @ForeignKey(referenceTable = "order", referenceColumn = "id", version = "1.0.0")
    private String orderId;

}
