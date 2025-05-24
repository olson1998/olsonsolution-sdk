package com.olsonsolution.common.spring.application.datasource.item.entity;

import com.olsonsolution.common.spring.application.annotation.migration.ChangeSet;
import com.olsonsolution.common.spring.application.datasource.model.audit.AuditableEntity;
import com.olsonsolution.common.spring.application.jpa.service.AuditableEntityListener;
import jakarta.persistence.*;
import lombok.*;
import org.joda.time.MutableDateTime;

@Getter
@Setter
@ToString(callSuper = true)

@NoArgsConstructor

@Entity
@ChangeSet
@Table(name = "category")
@EntityListeners({AuditableEntityListener.class})
public class CategoryData extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_id_seq")
    @SequenceGenerator(name = "category_id_seq", sequenceName = "category_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String fullName;

    @Column(name = "short_name", nullable = false, unique = true, length = 63)
    private String shortName;

    @Column(name = "description", nullable = false, length = 4095)
    private String description;

    @Builder(builderMethodName = "newCategory", builderClassName = "NewCategoryBuilder")
    public CategoryData(String fullName, String shortName, String description) {
        this.fullName = fullName;
        this.shortName = shortName;
        this.description = description;
    }

    @Builder(builderMethodName = "category")
    public CategoryData(MutableDateTime creationTimestamp, MutableDateTime lastUpdateTimestamp,
                        Long version, Long id, String fullName, String shortName, String description) {
        super(creationTimestamp, lastUpdateTimestamp, version);
        this.id = id;
        this.fullName = fullName;
        this.shortName = shortName;
        this.description = description;
    }
}
