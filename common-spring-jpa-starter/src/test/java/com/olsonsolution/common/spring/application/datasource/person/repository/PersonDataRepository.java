package com.olsonsolution.common.spring.application.datasource.person.repository;

import com.olsonsolution.common.spring.application.datasource.person.entity.PersonData;
import com.olsonsolution.common.spring.application.annotation.jpa.JpaSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@JpaSpec("Person")
public interface PersonDataRepository extends JpaRepository<PersonData, Long> {
}
