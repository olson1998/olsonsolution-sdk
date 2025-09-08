package com.olsonsolution.common.spring.application.datasource.item.entity;

import com.olsonsolution.common.spring.application.annotation.migration.ChangeSet;
import com.olsonsolution.common.spring.application.datasource.item.entity.support.ItemType;
import com.olsonsolution.common.spring.application.datasource.model.audit.AuditableEntity;
import com.olsonsolution.common.spring.application.jpa.service.AuditableEntityListener;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)

@NoArgsConstructor

@Entity
@ChangeSet
@Table(name = "item")
@EntityListeners(AuditableEntityListener.class)
public class ItemData extends AuditableEntity {

    @Id
    @Column(name = "id", length = 63, nullable = false, unique = true)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ItemType type;

    @Column(name = "packaging_id", length = 63)
    private String packagingId;

    @Column(name = "name", nullable = false)
    private String fullName;

    @Column(name = "short_name", length = 63, nullable = false)
    private String shortName;

    @Column(name = "description", length = 4095, nullable = false)
    private String description;

    @Column(name = "x_dimension", nullable = false)
    private Integer xDimension;

    @Column(name = "y_dimension", nullable = false)
    private Integer yDimension;

    @Column(name = "z_dimension", nullable = false)
    private Integer zDimension;

    @Column(name = "net_weight", nullable = false, precision = 15)
    private Double netWeight;

    @Column(name = "gross_weight", nullable = false, precision = 15)
    private Double grossWeight;

    @Builder(builderMethodName = "newItem", builderClassName = "NewItemBuilder")
    public ItemData(String id, ItemType type, String fullName, String description, Integer yDimension,
                    Double grossWeight, Integer zDimension, Double netWeight, Integer xDimension,
                    String shortName, String packagingId) {
        this.id = id;
        this.type = type;
        this.fullName = fullName;
        this.description = description;
        this.yDimension = yDimension;
        this.grossWeight = grossWeight;
        this.zDimension = zDimension;
        this.netWeight = netWeight;
        this.xDimension = xDimension;
        this.shortName = shortName;
        this.packagingId = packagingId;
    }

}
