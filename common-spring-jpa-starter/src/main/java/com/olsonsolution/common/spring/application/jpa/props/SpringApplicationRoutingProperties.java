package com.olsonsolution.common.spring.application.jpa.props;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.olsonsolution.common.spring.application.jpa.props.SpringApplicationJpaProperties.SPRING_APPLICATION_JPA_PROPERTIES_PREFIX;
import static com.olsonsolution.common.spring.application.jpa.props.SpringApplicationRoutingProperties.SPRING_APPLICATION_JPA_ROUTING_PROPERTIES_PREFIX;

@Data
@Configuration
@ConditionalOnMissingBean
@ConfigurationProperties(prefix = SPRING_APPLICATION_JPA_ROUTING_PROPERTIES_PREFIX)
public class SpringApplicationRoutingProperties implements DestinationDataSourceProvider {

    public static final String SPRING_APPLICATION_JPA_ROUTING_PROPERTIES_PREFIX =
            SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".routing";

    private final List<RoutingDataSourceProperties> instance = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoutingDataSourceProperties {

        private String name;

    }


    @Override
    public Optional<? extends SqlDataSource> findDestination(String dataSourceName) {
        return instance.stream()
                .filter(routingDataSourceProperties -> StringUtils.equalsIgnoreCase(
                        routingDataSourceProperties.name,
                        dataSourceName
                )).findFirst()
                .map(RoutingDataSourceProperties::getName);
    }
}
