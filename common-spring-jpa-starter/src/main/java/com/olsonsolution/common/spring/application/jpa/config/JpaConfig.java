package com.olsonsolution.common.spring.application.jpa.config;

import com.olsonsolution.common.spring.domain.port.props.jpa.JpaProperties;
import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaSpecConfigurer;
import com.olsonsolution.common.spring.domain.service.jpa.JpaSpecConfiguringService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaConfig {

    @Bean
    public JpaSpecConfigurer jpaSpecConfigurer(JpaProperties jpaProperties) {
        return new JpaSpecConfiguringService(jpaProperties);
    }

}
