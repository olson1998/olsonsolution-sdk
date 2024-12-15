package com.olsonsolution.common.spring.application.datasource.classic.repository;

import com.olsonsolution.common.spring.application.datasource.classic.entity.ClassicPersonTeamBoundData;
import com.olsonsolution.common.spring.application.datasource.classic.entity.embaddable.ClassicPersonTeamBound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassicPersonTeamBoundJpaRepository extends JpaRepository<ClassicPersonTeamBoundData, ClassicPersonTeamBound> {
}
