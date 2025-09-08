package com.olsonsolution.common.spring.application.datasource.item.repository;

import com.olsonsolution.common.spring.application.annotation.JpaSpec;
import com.olsonsolution.common.spring.application.datasource.item.entity.ItemCategoryData;
import com.olsonsolution.common.spring.application.datasource.item.entity.support.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@JpaSpec("WarehouseIndex")
public interface ItemCategoryJpaRepository extends JpaRepository<ItemCategoryData, ItemCategory> {
}
