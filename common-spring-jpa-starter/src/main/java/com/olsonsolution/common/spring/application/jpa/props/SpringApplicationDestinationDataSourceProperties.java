package com.olsonsolution.common.spring.application.jpa.props;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSourceUsers;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlDataSourceProvider;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.*;

import static com.olsonsolution.common.spring.application.jpa.config.DataSourceSpecConfig.H2_INITIAL_DATA_SOURCE;
import static com.olsonsolution.common.spring.application.jpa.config.DataSourceSpecConfig.H2_INITIAL_DATA_SOURCE_NAME;
import static com.olsonsolution.common.spring.application.jpa.props.SpringApplicationDestinationDataSourceProperties.SPRING_APPLICATION_JPA_DESTINATION_DATA_SOURCE_PROPERTIES_PREFIX;
import static com.olsonsolution.common.spring.application.jpa.props.SpringApplicationJpaProperties.SPRING_APPLICATION_JPA_PROPERTIES_PREFIX;

@Data
@Configuration
@ConfigurationProperties(prefix = SPRING_APPLICATION_JPA_DESTINATION_DATA_SOURCE_PROPERTIES_PREFIX)
public class SpringApplicationDestinationDataSourceProperties implements SqlDataSourceProvider {

    public static final String SPRING_APPLICATION_JPA_DESTINATION_DATA_SOURCE_PROPERTIES_PREFIX =
            SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".destination";

    private final List<RoutingDataSourceProperties> instance = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoutingDataSourceProperties {

        private String name;

        private final SqlDataSource dataSource = new ApplicationSqlDataSourceProperties();

    }


    @Override
    public Optional<? extends SqlDataSource> findDestination(String dataSourceName) {
        if(StringUtils.equals(dataSourceName, H2_INITIAL_DATA_SOURCE_NAME)) {
            return Optional.of(H2_INITIAL_DATA_SOURCE);
        } else {
            return instance.stream()
                    .filter(routingDataSourceProperties -> StringUtils.equalsIgnoreCase(
                            routingDataSourceProperties.name,
                            dataSourceName
                    )).findFirst()
                    .map(RoutingDataSourceProperties::getDataSource);
        }
    }

}
