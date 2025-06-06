package com.olsonsolution.common.spring.domain.port.repository.jpa;

import com.olsonsolution.common.spring.domain.port.repository.datasource.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlDataSourceProvider;
import lombok.NonNull;
import org.springframework.transaction.PlatformTransactionManager;

public interface JpaSpecConfigurer {

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
