package com.olsonsolution.common.spring.application.datasource.warehouse.entity;

import com.olsonsolution.common.spring.application.annotation.migration.ChangeSet;
import com.olsonsolution.common.spring.application.datasource.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)

@NoArgsConstructor
@AllArgsConstructor

@Entity
@ChangeSet
@Table(name = "warehouse")
public class WarehouseData extends AuditableEntity {

    @Id
    @SequenceGenerator(name = "warehouse_id_seq", sequenceName = "warehouse_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "name", length = 63, nullable = false, unique = true)
    private String name;

}
