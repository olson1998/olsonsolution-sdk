package com.olsonsolution.common.spring.domain.port.repository.datasource;

import com.olsonsolution.common.spring.domain.port.stereotype.datasource.JpaDataSourceSpec;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;

import javax.sql.DataSource;

public abstract class DestinationDataSourceManager
        extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl<JpaDataSourceSpec> {

    public abstract DataSource selectDataSourceBySpec(JpaDataSourceSpec jpaDataSourceSpec);

}
