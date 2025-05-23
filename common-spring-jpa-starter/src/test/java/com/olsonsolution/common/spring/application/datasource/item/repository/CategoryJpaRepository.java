package com.olsonsolution.common.spring.application.datasource.item.repository;

import com.olsonsolution.common.spring.application.annotation.jpa.JpaSpec;
import com.olsonsolution.common.spring.application.datasource.item.entity.CategoryData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@JpaSpec("ItemManagement")
public interface CategoryJpaRepository extends JpaRepository<CategoryData, Long> {
}
