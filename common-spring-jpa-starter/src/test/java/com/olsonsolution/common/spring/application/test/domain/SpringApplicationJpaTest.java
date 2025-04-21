package com.olsonsolution.common.spring.application.test.domain;

import com.olsonsolution.common.spring.application.datasource.organization.entity.CompanyData;
import com.olsonsolution.common.spring.application.datasource.organization.entity.OrganizationData;
import com.olsonsolution.common.spring.application.datasource.organization.entity.TeamData;
import com.olsonsolution.common.spring.application.datasource.organization.repository.CompanyDataRepository;
import com.olsonsolution.common.spring.application.datasource.organization.repository.OrganizationDataRepository;
import com.olsonsolution.common.spring.application.datasource.organization.repository.TeamDataRepository;
import com.olsonsolution.common.spring.application.datasource.person.entity.EmployeeData;
import com.olsonsolution.common.spring.application.datasource.person.entity.PersonData;
import com.olsonsolution.common.spring.application.datasource.person.repository.EmployeeDataRepository;
import com.olsonsolution.common.spring.application.datasource.person.repository.PersonDataRepository;
import com.olsonsolution.common.spring.application.test.config.SpringApplicationJpaTestBase;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

class SpringApplicationJpaTest extends SpringApplicationJpaTestBase {

    @Autowired
    private DataSourceSpecManager dataSourceSpecManager;
    @Autowired
    private OrganizationDataRepository organizationDataRepository;
    @Autowired
    private CompanyDataRepository companyDataRepository;
    @Autowired
    private TeamDataRepository teamDataRepository;
    @Autowired
    private PersonDataRepository personDataRepository;
    @Autowired
    private EmployeeDataRepository employeeDataRepository;

    @AfterEach
    void clearDataSourceSpec() {
        dataSourceSpecManager.clearThreadLocal();
    }

    @ParameterizedTest
    @MethodSource("com.olsonsolution.common.spring.application.test.config.SpringApplicationJpaTestBase#" +
            "dataSourceSpecStream")
    void shouldSaveTestData(DataSourceSpec spec) {
        dataSourceSpecManager.setThreadLocal(spec);
        Integer teamCode = saveMembershipData();
        savePersonData(teamCode);
    }

    @Transactional(transactionManager = "Membership_platformTransactionManager")
    Integer saveMembershipData() {
        OrganizationData organization = new OrganizationData(
                20,
                "Unit Test Organization lmtd.",
                "Unit Test Organization",
                "UTSTO",
                null
        );
        organization = organizationDataRepository.save(organization);
        CompanyData company = new CompanyData(
                100,
                organization.getCode(),
                "Unit Test Company lmtd.",
                "Unit Test Company",
                "UTSTC",
                null
        );
        company = companyDataRepository.save(company);
        TeamData team = new TeamData(
                2500,
                company.getCode(),
                "Unit Test Team lmtd.",
                "Unit Test Team",
                "UTSTT",
                null
        );
        return team.getCode();
    }

    @Transactional(transactionManager = "Person_platformTransactionManager")
    void savePersonData(Integer teamCode) {
        PersonData person = new PersonData(null, "John", null, "Doe", 'M');
        person = personDataRepository.save(person);
        EmployeeData employee = new EmployeeData(null, person.getId(), teamCode, UUID.randomUUID());
        employee = employeeDataRepository.save(employee);
    }

}
