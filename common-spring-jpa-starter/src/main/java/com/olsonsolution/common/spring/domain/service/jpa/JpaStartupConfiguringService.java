package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.spring.domain.model.datasource.DomainDataSourceSpec;
import com.olsonsolution.common.spring.domain.port.props.jpa.JpaProperties;
import com.olsonsolution.common.spring.domain.port.repository.datasource.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaStartupConfigurer;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationStartedEvent;

import java.util.Objects;

import static com.olsonsolution.common.data.domain.model.sql.SqlPermissions.RWX;

@RequiredArgsConstructor
public class JpaStartupConfiguringService implements JpaStartupConfigurer {

    private final JpaProperties jpaProperties;

    private final DataSourceSpecManager dataSourceSpecManager;

    @Override
    public void configure() {
        Objects.requireNonNull(jpaProperties.getDefaultDataSource(), "Default data source name must be provided");
        DataSourceSpec dataSourceSpec = new DomainDataSourceSpec(jpaProperties.getDefaultDataSource(), RWX);
        dataSourceSpecManager.setThreadLocal(dataSourceSpec);
    }

    @Override
    public void configure(ApplicationStartedEvent event) {

    }
}
