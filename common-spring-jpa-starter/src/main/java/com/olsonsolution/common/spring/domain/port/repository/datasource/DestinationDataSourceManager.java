package com.olsonsolution.common.spring.domain.port.repository.datasource;

import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpecification;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;

import javax.sql.DataSource;

public abstract class DestinationDataSourceManager
        extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl<DataSourceSpecification> {

    public abstract DataSource selectDataSourceBySpec(DataSourceSpecification dataSourceSpecification);

}
