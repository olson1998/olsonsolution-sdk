package com.olsonsolution.common.spring.application.datasource.item.entity;

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
@Table(name = "category")
public class CategoryData extends AuditableEntity {

    @Id
    @SequenceGenerator(name = "category_id_seq", sequenceName = "category_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "full_name", nullable = false, unique = true)
    private String fullName;

    @Column(name = "full_name", nullable = false, unique = true, length = 63)
    private String shortName;

    @Column(name = "description", nullable = false, length = 4095)
    private String description;

}
