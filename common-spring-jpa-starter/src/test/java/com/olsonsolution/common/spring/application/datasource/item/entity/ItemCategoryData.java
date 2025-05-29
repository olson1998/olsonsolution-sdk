package com.olsonsolution.common.spring.application.datasource.item.entity;

import com.olsonsolution.common.spring.application.annotation.migration.ChangeSet;
import com.olsonsolution.common.spring.application.datasource.item.entity.support.ItemCategory;
import com.olsonsolution.common.spring.application.datasource.model.audit.AuditableEntity;
import com.olsonsolution.common.spring.application.jpa.service.AuditableEntityListener;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)

@NoArgsConstructor

@Entity
@ChangeSet
@Table(name = "item_category_bound")
@EntityListeners(AuditableEntityListener.class)
public class ItemCategoryData extends AuditableEntity {

    @EmbeddedId
    private ItemCategory bound;

    public ItemCategoryData(ItemCategory bound) {
        this.bound = bound;
    }

}
