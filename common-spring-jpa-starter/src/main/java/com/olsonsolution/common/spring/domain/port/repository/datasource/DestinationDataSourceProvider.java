package com.olsonsolution.common.spring.domain.port.repository.datasource;

import com.olsonsolution.common.spring.domain.port.stereotype.hibernate.DataBaseEnvironment;
import com.zaxxer.hikari.HikariConfig;

import java.util.Optional;

public interface DestinationDataSourceProvider {

    DataBaseEnvironment getProductDataSourceEnvironment();

    Optional<HikariConfig> findDestinationConfig(DataBaseEnvironment dataBaseEnvironment);

}
