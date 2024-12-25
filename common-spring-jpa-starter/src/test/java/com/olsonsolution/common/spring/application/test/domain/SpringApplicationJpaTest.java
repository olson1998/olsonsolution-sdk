package com.olsonsolution.common.spring.application.test.domain;

import com.olsonsolution.common.spring.application.datasource.classic.entity.ClassicPersonData;
import com.olsonsolution.common.spring.application.datasource.classic.entity.ClassicPersonTeamBoundData;
import com.olsonsolution.common.spring.application.datasource.classic.entity.ClassicTeamData;
import com.olsonsolution.common.spring.application.datasource.classic.entity.embaddable.ClassicPersonTeamBound;
import com.olsonsolution.common.spring.application.datasource.classic.repository.ClassicPersonJpaRepository;
import com.olsonsolution.common.spring.application.datasource.classic.repository.ClassicPersonTeamBoundJpaRepository;
import com.olsonsolution.common.spring.application.datasource.classic.repository.ClassicTeamJpaRepository;
import com.olsonsolution.common.spring.application.test.config.SpringApplicationJpaTestBase;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

class SpringApplicationJpaTest extends SpringApplicationJpaTestBase {

    @Autowired
    private DataSourceSpecManager dataSourceSpecManager;

    @Autowired
    private ClassicTeamJpaRepository classicTeamJpaRepository;

    @Autowired
    private ClassicPersonJpaRepository classicPersonJpaRepository;

    @Autowired
    private ClassicPersonTeamBoundJpaRepository classicPersonTeamBoundJpaRepository;

    @AfterEach
    void clearDataSourceSpec() {
        dataSourceSpecManager.clearThreadLocal();
    }

    @ParameterizedTest
    @MethodSource("com.olsonsolution.common.spring.application.test.config.SpringApplicationJpaTestBase" +
            "#dataSourceSpecStream")
    void shouldSaveTestData(DataSourceSpec spec) {
        dataSourceSpecManager.setThreadLocal(spec);
        saveTestData();
    }

    @Transactional
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
        assertThat(classicTeamJpaRepository.existsById(persistedTeam.getId())).isTrue();
        assertThat( classicPersonJpaRepository.existsById(persistedPerson.getId())).isTrue();
        assertThat(classicPersonTeamBoundJpaRepository.existsById(persistedBound.getValueMap())).isTrue();
        classicPersonTeamBoundJpaRepository.deleteById(persistedBound.getValueMap());
        classicPersonJpaRepository.deleteById(persistedPerson.getId());
        classicTeamJpaRepository.deleteById(persistedTeam.getId());
        assertThat(classicTeamJpaRepository.existsById(persistedTeam.getId())).isFalse();
        assertThat( classicPersonJpaRepository.existsById(persistedPerson.getId())).isFalse();
        assertThat(classicPersonTeamBoundJpaRepository.existsById(persistedBound.getValueMap())).isFalse();
    }

}
