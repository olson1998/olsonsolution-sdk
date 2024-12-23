package com.olsonsolution.common.spring.application.jpa.props;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSourceUsers;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceProvider;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.stream.Collectors;

import static com.olsonsolution.common.spring.application.jpa.props.SpringApplicationDestinationDataSourceProperties.SPRING_APPLICATION_JPA_DESTINATION_DATA_SOURCE_PROPERTIES_PREFIX;
import static com.olsonsolution.common.spring.application.jpa.props.SpringApplicationJpaProperties.SPRING_APPLICATION_JPA_PROPERTIES_PREFIX;
import static java.lang.reflect.Modifier.PRIVATE;

@Data
@Configuration
@ConfigurationProperties(prefix = SPRING_APPLICATION_JPA_DESTINATION_DATA_SOURCE_PROPERTIES_PREFIX)
public class SpringApplicationDestinationDataSourceProperties implements DestinationDataSourceProvider {

    public static final String SPRING_APPLICATION_JPA_DESTINATION_DATA_SOURCE_PROPERTIES_PREFIX =
            SPRING_APPLICATION_JPA_PROPERTIES_PREFIX + ".destination";

    private final List<RoutingDataSourceProperties> instance = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoutingDataSourceProperties {

        private String name;

        private final SqlDataSource dataSource = new DataSourceProperties();

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataSourceProperties implements SqlDataSource {

        private SqlVendor vendor;

        private String host;

        private Integer port;

        private String database;

        private final UsersProperties user = new UsersProperties();

        private final List<DataSourceProperty> property = new ArrayList<>();

        @Override
        public SqlDataSourceUsers getUsers() {
            return user;
        }

        @Override
        public Map<String, String> getProperties() {
            Map<String, String> properties = new HashMap<>(property.size());
            for(var propertyValue : property) {
                properties.put(propertyValue.name, propertyValue.value);
            }
           return properties;
        }

    }

    @Data
    public static class UsersProperties implements SqlDataSourceUsers {

        private final List<UserProperties> ro = new ArrayList<>();

        private final List<UserProperties> wo = new ArrayList<>();

        private final List<UserProperties> rw = new ArrayList<>();

        private final List<UserProperties> rwx = new ArrayList<>();

        @Override
        public List<? extends SqlUser> getReadOnly() {
            return ro;
        }

        @Override
        public List<? extends SqlUser> getWriteOnly() {
            return wo;
        }

        @Override
        public List<? extends SqlUser> getReadWrite() {
            return rw;
        }

        @Override
        public List<? extends SqlUser> getReadWriteExecute() {
            return rwx;
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProperties implements SqlUser {

        private String username;

        private String password;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataSourceProperty {

        private String name;

        private String value;

    }

}
