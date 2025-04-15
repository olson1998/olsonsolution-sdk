package com.olsonsolution.common.spring.application.datasource.modern.repository;

import com.olsonsolution.common.spring.application.datasource.modern.entity.PersonTeamBoundData;
import com.olsonsolution.common.spring.application.datasource.modern.entity.embaddable.PersonTeamBound;
import com.olsonsolution.common.spring.domain.model.annotation.JpaSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@JpaSpec("Modern")
public interface PersonTeamBoundJpaRepository extends JpaRepository<PersonTeamBoundData, PersonTeamBound> {
}
