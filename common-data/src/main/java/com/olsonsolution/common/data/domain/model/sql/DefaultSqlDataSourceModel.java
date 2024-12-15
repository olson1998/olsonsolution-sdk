package com.olsonsolution.common.data.domain.model.sql;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Builder(builderMethodName = "sqlDataSource")
public class DefaultSqlDataSourceModel extends SqlDataSourceModel<SqlVendors, DefaultSqlDataSourceUsersModel> {

}
