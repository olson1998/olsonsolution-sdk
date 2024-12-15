package com.olsonsolution.common.spring.application.datasource.modern.repository;

import com.olsonsolution.common.spring.application.datasource.modern.entity.PersonData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonJpaRepository extends JpaRepository<PersonData, Long> {
}
