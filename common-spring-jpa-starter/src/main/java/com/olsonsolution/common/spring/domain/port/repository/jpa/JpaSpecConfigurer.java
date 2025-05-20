package com.olsonsolution.common.spring.domain.port.repository.jpa;

import com.olsonsolution.common.spring.domain.port.repository.datasource.DestinationDataSourceManager;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlDataSourceProvider;
import lombok.NonNull;

public interface JpaSpecConfigurer {

    boolean resolveCreateSchema(@NonNull String jpaSpec);

    String resolveSchema(@NonNull String jpaSpec);

    EntityManagerFactoryDelegate createEntityManagerFactoryDelegate(
            @NonNull String jpaSpecName,
            @NonNull String[] entityBasePackages,
            DataSourceSpecManager dataSourceSpecManager,
            SqlDataSourceProvider sqlDataSourceProvider,
            DestinationDataSourceManager destinationDataSourceManager);

    PlatformTransactionManagerDelegate createPlatformTransactionManagerDelegate(
            DataSourceSpecManager dataSourceSpecManager,
            SqlDataSourceProvider sqlDataSourceProvider,
            EntityManagerFactoryDelegate entityManagerFactoryDelegate);

}
