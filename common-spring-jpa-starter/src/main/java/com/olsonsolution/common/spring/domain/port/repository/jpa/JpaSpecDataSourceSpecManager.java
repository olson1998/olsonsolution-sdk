package com.olsonsolution.common.spring.domain.port.repository.jpa;

import com.olsonsolution.common.spring.domain.port.async.repository.ThreadLocalAware;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.JpaDataSourceSpec;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public interface JpaSpecDataSourceSpecManager extends ThreadLocalAware<JpaDataSourceSpec>,
        CurrentTenantIdentifierResolver<JpaDataSourceSpec> {

}
