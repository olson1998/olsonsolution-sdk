package com.olsonsolution.common.spring.application.datasource.classic.repository;

import com.olsonsolution.common.spring.application.datasource.classic.entity.ClassicTeamData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassicTeamJpaRepository extends JpaRepository<ClassicTeamData, Long> {
}
