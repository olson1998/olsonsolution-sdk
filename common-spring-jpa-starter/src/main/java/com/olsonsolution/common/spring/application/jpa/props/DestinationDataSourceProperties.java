package com.olsonsolution.common.spring.application.jpa.props;

import com.olsonsolution.common.data.domain.model.sql.DefaultSqlDataSourceModel;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
@ConfigurationProperties(prefix = "common.spring.application.jpa.routing")
public class DestinationDataSourceProperties implements DestinationDataSourceProvider {

    private final List<RoutingDataSourceProperties> instance = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoutingDataSourceProperties {

        private String name;

        private final DefaultSqlDataSourceModel dataSource = new DefaultSqlDataSourceModel();

    }

    @Override
    public Optional<? extends SqlDataSource> findDefaultDestination() {
        return Optional.empty();
    }

    @Override
    public Optional<? extends SqlDataSource> findDestination(String dataSourceName) {
        return instance.stream()
                .filter(routingDataSourceProperties -> StringUtils.equalsIgnoreCase(
                        routingDataSourceProperties.name,
                        dataSourceName
                )).findFirst()
                .map(RoutingDataSourceProperties::getDataSource);
    }
}
