package com.olsonsolution.common.spring.application.datasource.item.entity;

import com.olsonsolution.common.spring.application.annotation.migration.ChangeSet;
import com.olsonsolution.common.spring.application.datasource.common.entity.AuditableEntity;
import com.olsonsolution.common.spring.application.datasource.item.entity.support.ItemType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)

@NoArgsConstructor
@AllArgsConstructor

@Entity
@ChangeSet
@Table(name = "item")
public class ItemData extends AuditableEntity {

    @Id
    @Column(name = "id", length = 63, nullable = false, unique = true)
    private String id;

    @Column(name = "type", nullable = false)
    private ItemType type;

    @Column(name = "packagingId_id", length = 63)
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

}
