package com.olsonsolution.common.spring.application.test.config;

import com.olsonsolution.common.spring.application.caching.InMemoryCachingConfig;
import com.olsonsolution.common.spring.application.jpa.config.DataSourceSpecConfig;
import com.olsonsolution.common.spring.application.jpa.config.RoutingJpaConfigurer;
import com.olsonsolution.common.spring.application.jpa.props.ApplicationJpaProperties;
import com.olsonsolution.common.spring.application.jpa.props.DestinationDataSourceProperties;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties
@ContextConfiguration(classes = {
        DataSourceSpecConfig.class,
        RoutingJpaConfigurer.class,
        InMemoryCachingConfig.class,
        ApplicationJpaProperties.class,
        DestinationDataSourceProperties.class
})
@TestPropertySource(locations = "classpath://application-spring-common.properties")
@ExtendWith(SpringExtension.class)
public abstract class JpaDataTestBase {

}
