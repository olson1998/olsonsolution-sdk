package com.olsonsolution.common.spring.application.datasource.item.entity.support;

import com.olsonsolution.common.spring.application.annotation.migration.ForeignKey;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Embeddable
public class ItemCategory {

    @Column(name = "category_id", nullable = false)
    @ForeignKey(referenceTable = "category", referenceColumn = "id", version = "1.0.0")
    private Long categoryId;

    @Column(name = "item_id", nullable = false, length = 63)
    @ForeignKey(referenceTable = "item", referenceColumn = "id", version = "1.0.0")
    private String itemId;

}
