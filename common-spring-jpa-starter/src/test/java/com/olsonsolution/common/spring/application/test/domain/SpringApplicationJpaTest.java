package com.olsonsolution.common.spring.application.test.domain;

import com.olsonsolution.common.spring.application.test.config.SpringApplicationJpaTestBase;
import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

class SpringApplicationJpaTest extends SpringApplicationJpaTestBase {

    @Autowired
    private DataSourceSpecManager dataSourceSpecManager;

    @AfterEach
    void clearDataSourceSpec() {
        dataSourceSpecManager.clear();
    }

    @ParameterizedTest
    @MethodSource("com.olsonsolution.common.spring.application.test.config.SpringApplicationJpaTestBase#" +
            "dataSourceSpecStream")
    void shouldSaveTestData(DataSourceSpec spec) {

    }

}
