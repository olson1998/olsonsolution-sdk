package com.olsonsolution.common.spring.application.datasource.item.entity.support;

import com.olsonsolution.common.spring.application.annotation.migration.ForeignKey;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ItemCategory {

    @Column(name = "category_id", nullable = false, unique = true)
    @ForeignKey(referenceTable = "category", referenceColumn = "id", version = "1.0.0")
    private Long categoryId;

    @Column(name = "item_id", nullable = false, unique = true, length = 63)
    @ForeignKey(referenceTable = "item", referenceColumn = "id", version = "1.0.0")
    private String itemId;

}
