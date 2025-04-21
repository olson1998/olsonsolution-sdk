package com.olsonsolution.common.spring.application.datasource.person.repository;

import com.olsonsolution.common.spring.application.datasource.person.entity.EmployeeData;
import com.olsonsolution.common.spring.application.annotation.jpa.JpaSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@JpaSpec("Person")
public interface EmployeeDataRepository extends JpaRepository<EmployeeData, Long> {
}
