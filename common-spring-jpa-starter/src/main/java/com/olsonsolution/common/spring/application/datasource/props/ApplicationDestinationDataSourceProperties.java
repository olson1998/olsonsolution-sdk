package com.olsonsolution.common.spring.application.datasource.props;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.spring.domain.port.props.datasource.DestinationDataSourceProperties;
import com.olsonsolution.common.spring.domain.port.props.datasource.SqlDataSourceProperties;
import com.olsonsolution.common.spring.domain.port.props.datasource.SqlUsersProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.application.data-source")
@ConditionalOnProperty(name = "spring.application.data-source.provider", havingValue = "properties")
public class ApplicationDestinationDataSourceProperties implements DestinationDataSourceProperties {

    private final List<ApplicationSqlDataSourceProperties> instance = new ArrayList<>();

    @Override
    public Collection<? extends SqlDataSourceProperties> getInstances() {
        return instance;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationSqlDataSourceProperties implements SqlDataSourceProperties {

        private String name;

        private SqlVendor vendor;

        private String host;

        private Integer port;

        private String database;

        private final List<ApplicationSqlUsersProperties> user = new ArrayList<>();

        private final Properties properties = new Properties();

        @Override
        public List<? extends SqlUsersProperties> getUsers() {
            return user;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationSqlUsersProperties implements SqlUsersProperties {

        private String schema;

        private final SqlUser readOnly = new ApplicationSqlUserProperties();

        private final SqlUser writeOnly = new ApplicationSqlUserProperties();

        private final SqlUser readWrite = new ApplicationSqlUserProperties();

        private final SqlUser readWriteExecute = new ApplicationSqlUserProperties();

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationSqlUserProperties implements SqlUser {

        private String username;

        private String password;

    }

}
