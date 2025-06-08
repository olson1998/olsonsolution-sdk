package com.olsonsolution.common.spring.application.jpa.config;

import com.olsonsolution.common.spring.domain.port.props.jpa.JpaProperties;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaSpecConfigurer;
import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaStartupConfigurer;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.JpaDataSourceSpec;
import com.olsonsolution.common.spring.domain.service.jpa.JpaSpecConfiguringService;
import com.olsonsolution.common.spring.domain.service.jpa.JpaStartupConfiguringService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaConfig {

    public static final String JPA_SPEC_CONFIGURER_BEAN = "jpaSpecConfigurer";

    public static final String DEFAULT_JPA_DATA_SOURCE_SPEC_BEAN = "defaultJpaDataSourceSpec";

    @Bean(JPA_SPEC_CONFIGURER_BEAN)
    public JpaSpecConfigurer jpaSpecConfigurer(JpaProperties jpaProperties,
                                               JpaStartupConfigurer jpaStartupConfigurer) {
        jpaStartupConfigurer.configure();
        return new JpaSpecConfiguringService(jpaProperties);
    }

    @Bean(DEFAULT_JPA_DATA_SOURCE_SPEC_BEAN)
    public JpaDataSourceSpec defaultJpaDataSourceSpec(JpaSpecConfigurer jpaSpecConfigurer) {
        return jpaSpecConfigurer.getDefaultJpaDataSourceSpec();
    }

    @Bean
    public JpaStartupConfigurer jpaStartupConfigurer(JpaProperties jpaProperties,
                                                     DataSourceSpecManager dataSourceSpecManager) {
        return new JpaStartupConfiguringService(jpaProperties, dataSourceSpecManager);
    }

}
