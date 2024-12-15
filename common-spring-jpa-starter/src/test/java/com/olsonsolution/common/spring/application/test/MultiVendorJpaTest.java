package com.olsonsolution.common.spring.application.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableJpaRepositories(
        basePackages = {
                "com.olsonsolution.common.spring.application.datasource.classic.repository",
                "com.olsonsolution.common.spring.application.datasource.modern.repository"
        },
        entityManagerFactoryRef = "routingEntityManagerFactory",
        transactionManagerRef = "routingPlatformTransactionManager"
)
@EnableConfigurationProperties
@ComponentScan(basePackages = "com.olsonsolution.common.spring.application")
@ContextConfiguration(classes = MultiVendorJpaTest.class)
@TestPropertySource(locations = "classpath://application-spring-common.properties")
@ExtendWith(SpringExtension.class)
class MultiVendorJpaTest {

    @Test
    void shouldBuild() {

    }

}
