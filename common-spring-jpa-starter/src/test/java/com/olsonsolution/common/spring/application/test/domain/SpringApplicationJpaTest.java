package com.olsonsolution.common.spring.application.test.domain;

import com.olsonsolution.common.spring.application.datasource.classic.entity.ClassicPersonData;
import com.olsonsolution.common.spring.application.datasource.classic.entity.ClassicPersonTeamBoundData;
import com.olsonsolution.common.spring.application.datasource.classic.entity.ClassicTeamData;
import com.olsonsolution.common.spring.application.datasource.classic.entity.embaddable.ClassicPersonTeamBound;
import com.olsonsolution.common.spring.application.datasource.classic.repository.ClassicPersonJpaRepository;
import com.olsonsolution.common.spring.application.datasource.classic.repository.ClassicPersonTeamBoundJpaRepository;
import com.olsonsolution.common.spring.application.datasource.classic.repository.ClassicTeamJpaRepository;
import com.olsonsolution.common.spring.application.jpa.config.RoutingJpaConfigurer;
import com.olsonsolution.common.spring.application.test.config.SpringApplicationJpaTestBase;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecConfigurable;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

class SpringApplicationJpaTest extends SpringApplicationJpaTestBase {

    @Autowired
    private DataSourceSpecManager dataSourceSpecManager;

    @Autowired
    private ClassicTeamJpaRepository classicTeamJpaRepository;

    @Autowired
    private ClassicPersonJpaRepository classicPersonJpaRepository;

    @Autowired
    private ClassicPersonTeamBoundJpaRepository classicPersonTeamBoundJpaRepository;

    @Autowired
    private List<DataSourceSpecConfigurable<?>> dataSourceSpecConfigurableList;

    @AfterEach
    void clearDataSourceSpec() {
        dataSourceSpecConfigurableList.forEach(DataSourceSpecConfigurable::clear);
        dataSourceSpecManager.clear();
    }

    @ParameterizedTest
    @MethodSource("com.olsonsolution.common.spring.application.test.config.SpringApplicationJpaTestBase" +
            "#dataSourceSpecStream")
    void shouldSaveTestData(DataSourceSpec spec) {
        dataSourceSpecManager.setCurrent(spec);
        saveTestData();
    }

    @Transactional(transactionManager = "COMPANY_platformTransactionManager")
    void saveTestData() {
        ClassicTeamData classicTeamData = new ClassicTeamData(1L, "TEAM1", "team1");
        ClassicPersonData classicPersonData = new ClassicPersonData(1L, "John", "Doe", "M");
        ClassicPersonTeamBoundData bound = new ClassicPersonTeamBoundData(new ClassicPersonTeamBound(
                1L,
                1L
        ));
        ClassicTeamData persistedTeam = classicTeamJpaRepository.save(classicTeamData);
        ClassicPersonData persistedPerson = classicPersonJpaRepository.save(classicPersonData);
        ClassicPersonTeamBoundData persistedBound = classicPersonTeamBoundJpaRepository.save(bound);
        classicTeamJpaRepository.existsById(persistedTeam.getId());
        classicPersonJpaRepository.existsById(persistedPerson.getId());
        classicPersonTeamBoundJpaRepository.existsById(persistedBound.getValueMap());
    }

}
