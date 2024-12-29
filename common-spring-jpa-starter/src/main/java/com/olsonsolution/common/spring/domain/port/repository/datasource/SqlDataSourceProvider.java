package com.olsonsolution.common.spring.domain.port.repository.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;

import java.util.Optional;

public interface SqlDataSourceProvider {

    Optional<? extends SqlDataSource> findDestination(String dataSourceName);

}
