package com.olsonsolution.common.spring.domain.port.repository.datasource;

import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;

import javax.sql.DataSource;

public abstract class RoutingDataSourceManager extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl<DataSourceSpec> {

    public abstract DataSource selectDataSourceBySpec(DataSourceSpec dataSourceSpec);

}
