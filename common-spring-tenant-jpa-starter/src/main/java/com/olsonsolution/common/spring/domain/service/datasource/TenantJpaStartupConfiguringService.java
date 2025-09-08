package com.olsonsolution.common.spring.domain.service.datasource;

import com.olsonsolution.common.spring.domain.model.context.SystemContextType;
import com.olsonsolution.common.spring.domain.model.context.TenantThreadLocalContext;
import com.olsonsolution.common.spring.domain.model.context.ThreadLocalContextMetadata;
import com.olsonsolution.common.spring.domain.model.tenant.DomainTenant;
import com.olsonsolution.common.spring.domain.port.props.jpa.JpaProperties;
import com.olsonsolution.common.spring.domain.port.repository.context.LocalContextManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaStartupConfigurer;
import com.olsonsolution.common.spring.domain.port.stereotype.context.LocalContext;
import com.olsonsolution.common.time.domain.port.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationStartedEvent;

import java.util.Objects;

@RequiredArgsConstructor
public class TenantJpaStartupConfiguringService implements JpaStartupConfigurer {

    private final TimeUtils timeUtils;

    private final JpaProperties jpaProperties;

    private final LocalContextManager localContextManager;

    @Override
    public void configure() {
        String tenantId = jpaProperties.getDefaultDataSource();
        tenantId = StringUtils.substringBefore(tenantId, "_DataSource");
        Objects.requireNonNull(jpaProperties.getDefaultDataSource(), tenantId);
        LocalContext localContext = TenantThreadLocalContext.tenantContextBuilder()
                .tenant(new DomainTenant(tenantId))
                .metadata(ThreadLocalContextMetadata.builder()
                        .startTimestamp(timeUtils.getTimestamp())
                        .build())
                .id("startup")
                .type(SystemContextType.SYSTEM)
                .build();
        localContextManager.setThreadLocal(localContext);
    }

    @Override
    public void configure(ApplicationStartedEvent event) {

    }
}
