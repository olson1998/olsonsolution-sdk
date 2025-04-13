package com.olsonsolution.common.spring.application.config.jpa.test;

import com.olsonsolution.common.spring.domain.model.annotation.EnableJpaSpec;
import com.olsonsolution.common.spring.domain.model.annotation.JpaSpec;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableJpaSpec({
        @JpaSpec(
                value = "Classic",
                repositoriesPackages = "com.olsonsolution.common.spring.application.datasource.classic.repository"
        ),
        @JpaSpec(
                value = "Modern",
                repositoriesPackages = "com.olsonsolution.common.spring.application.datasource.modern.repository"
        )
})
public class TestJpaSpecConfig {
}
