package com.olsonsolution.common.spring.application.datasource.organization.repository;

import com.olsonsolution.common.spring.application.datasource.organization.entity.TeamData;
import com.olsonsolution.common.spring.application.annotation.jpa.JpaSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@JpaSpec("Membership")
public interface TeamDataRepository extends JpaRepository<TeamData, Integer> {
}
