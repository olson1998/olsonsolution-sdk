package com.olsonsolution.common.spring.application.datasource.item.entity;

import com.olsonsolution.common.spring.application.annotation.migration.ChangeSet;
import com.olsonsolution.common.spring.application.datasource.common.entity.AuditableEntity;
import com.olsonsolution.common.spring.application.datasource.item.entity.support.ItemCategory;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)

@NoArgsConstructor
@AllArgsConstructor

@Entity
@ChangeSet
@Table(name = "item_category_bound")
public class ItemCategoryData extends AuditableEntity {

    @EmbeddedId
    private ItemCategory bound;

}
