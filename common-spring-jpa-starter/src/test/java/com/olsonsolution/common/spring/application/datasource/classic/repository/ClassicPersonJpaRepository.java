package com.olsonsolution.common.spring.application.datasource.classic.repository;

import com.olsonsolution.common.spring.application.datasource.classic.entity.ClassicPersonData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassicPersonJpaRepository extends JpaRepository<ClassicPersonData, Long> {

    @Query(
           """
           SELECT person
           FROM ClassicPersonData person
           LEFT OUTER JOIN ClassicPersonTeamBoundData teamBound
           ON person.id = teamBound.valueMap.personId
           LEFT OUTER JOIN ClassicTeamData team
           ON team.id = teamBound.valueMap.teamId
           WHERE team.id = :teamId
           """)
    List<ClassicPersonData> selectUserByTeamId(@Param("teamId") Long teamId);

}
