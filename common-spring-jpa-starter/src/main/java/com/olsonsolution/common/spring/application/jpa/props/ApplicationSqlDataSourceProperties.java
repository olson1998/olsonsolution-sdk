package com.olsonsolution.common.spring.application.jpa.props;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSourceUsers;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSqlDataSourceProperties implements SqlDataSource {

    private SqlVendor vendor;

    private String host;

    private Integer port;

    private String database;

    private final ApplicationSqlDataSourceUsersProperties user = new ApplicationSqlDataSourceUsersProperties();

    private final Set<ApplicationSqlDataSourceProperty> property = new HashSet<>();

    @Override
    public SqlDataSourceUsers getUsers() {
        return user;
    }

    @Override
    public Map<String, String> getProperties() {
        return property.stream()
                .collect(Collectors.toUnmodifiableMap(
                        ApplicationSqlDataSourceProperty::getName,
                        ApplicationSqlDataSourceProperty::getValue
                ));
    }
}
