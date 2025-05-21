package com.olsonsolution.common.spring.application.datasource.organization.repository;

import com.olsonsolution.common.spring.application.annotation.jpa.JpaSpec;
import com.olsonsolution.common.spring.application.datasource.organization.entity.CellData;
import com.olsonsolution.common.spring.application.datasource.organization.entity.embadabble.Cell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@JpaSpec("Membership")
public interface CellDataRepository extends JpaRepository<CellData, Cell> {
}
