package com.olsonsolution.common.spring.application.datasource.config;

import com.olsonsolution.common.spring.application.datasource.props.ApplicationDestinationDataSourceProperties;
import com.olsonsolution.common.spring.domain.port.props.datasource.DestinationDataSourceProperties;
import com.olsonsolution.common.spring.domain.port.props.datasource.SqlVendorSupportProperties;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaSpecConfigurer;
import com.olsonsolution.common.spring.domain.service.datasource.DestinationDataSourcePropertyLookupService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(ApplicationDestinationDataSourceProperties.class)
public class DestinationDataSourcePropertyLookupServiceConfig {

    @Bean
    public SqlDataSourceProvider sqlDataSourceProvider(
            JpaSpecConfigurer jpaSpecConfigurer,
            SqlVendorSupportProperties sqlVendorSupportProperties,
            DestinationDataSourceProperties destinationDataSourceProperties) {
        return new DestinationDataSourcePropertyLookupService(
                jpaSpecConfigurer,
                sqlVendorSupportProperties,
                destinationDataSourceProperties
        );
    }

}
