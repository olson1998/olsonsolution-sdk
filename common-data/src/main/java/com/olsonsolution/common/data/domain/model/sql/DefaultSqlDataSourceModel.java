package com.olsonsolution.common.data.domain.model.sql;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Properties;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DefaultSqlDataSourceModel extends SqlDataSourceModel<SqlVendors, DefaultSqlDataSourceUsersModel> {

    @Builder(builderMethodName = "sqlDataSource")
    public DefaultSqlDataSourceModel(SqlVendors vendor,
                                     String host,
                                     Integer port,
                                     String database,
                                     Properties properties,
                                     DefaultSqlDataSourceUsersModel users) {
        super(vendor, host, port, database, properties, users);
    }

    public DefaultSqlDataSourceModel() {
    }
}
