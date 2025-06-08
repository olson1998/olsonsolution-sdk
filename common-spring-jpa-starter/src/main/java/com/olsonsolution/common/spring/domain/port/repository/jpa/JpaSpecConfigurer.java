package com.olsonsolution.common.spring.domain.port.repository.jpa;

import com.olsonsolution.common.spring.domain.port.repository.datasource.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlDataSourceProvider;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.JpaDataSourceSpec;
import lombok.NonNull;
import org.springframework.transaction.PlatformTransactionManager;

public interface JpaSpecConfigurer {

    JpaDataSourceSpec getDefaultJpaDataSourceSpec();

    boolean resolveCreateSchema(@NonNull String jpaSpec);

    String resolveSchema(@NonNull String jpaSpec);

    EntityManagerFactoryDelegate createEntityManagerFactoryDelegate(
            @NonNull String jpaSpecName,
            @NonNull String[] entityBasePackages,
            DataSourceSpecManager dataSourceSpecManager,
            JpaSpecDataSourceSpecManager jpaSpecDataSourceSpecManager,
            SqlDataSourceProvider sqlDataSourceProvider,
            DestinationDataSourceManager destinationDataSourceManager);

    PlatformTransactionManager createPlatformTransactionManager(EntityManagerFactoryDelegate em);

}
