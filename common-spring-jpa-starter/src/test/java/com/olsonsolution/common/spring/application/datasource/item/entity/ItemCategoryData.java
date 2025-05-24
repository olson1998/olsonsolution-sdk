package com.olsonsolution.common.spring.application.datasource.item.entity;

import com.olsonsolution.common.spring.application.annotation.migration.ChangeSet;
import com.olsonsolution.common.spring.application.config.jpa.TimeAuditingEntityListener;
import com.olsonsolution.common.spring.application.datasource.common.entity.AuditableEntity;
import com.olsonsolution.common.spring.application.datasource.item.entity.support.ItemCategory;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.*;
import org.joda.time.MutableDateTime;

@Getter
@Setter
@ToString(callSuper = true)

@NoArgsConstructor

@Entity
@ChangeSet
@Table(name = "item_category_bound")
@EntityListeners(TimeAuditingEntityListener.class)
public class ItemCategoryData extends AuditableEntity {

    @EmbeddedId
    private ItemCategory bound;

    public ItemCategoryData(ItemCategory bound) {
        this.bound = bound;
    }

    @Builder(builderMethodName = "itemCategory")
    public ItemCategoryData(MutableDateTime creationTimestamp, MutableDateTime lastUpdateTimestamp,
                            Long version, ItemCategory bound) {
        super(creationTimestamp, lastUpdateTimestamp, version);
        this.bound = bound;
    }
}
