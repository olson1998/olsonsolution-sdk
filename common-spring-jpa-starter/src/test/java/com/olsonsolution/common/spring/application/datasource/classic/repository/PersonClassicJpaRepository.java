package com.olsonsolution.common.spring.application.datasource.classic.repository;

import com.olsonsolution.common.spring.application.datasource.classic.entity.PersonClassicData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonClassicJpaRepository extends JpaRepository<PersonClassicData, Long> {
}
