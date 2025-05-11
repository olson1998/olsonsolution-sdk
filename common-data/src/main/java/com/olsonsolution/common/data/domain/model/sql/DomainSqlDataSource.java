package com.olsonsolution.common.data.domain.model.sql;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSourceUsers;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import lombok.*;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainSqlDataSource implements SqlDataSource {

    private SqlVendor vendor;

    private String host;

    private Integer port;

    private String database;

    private SqlDataSourceUsers users;

    @Singular("property")
    private Map<String, String> properties;

}
