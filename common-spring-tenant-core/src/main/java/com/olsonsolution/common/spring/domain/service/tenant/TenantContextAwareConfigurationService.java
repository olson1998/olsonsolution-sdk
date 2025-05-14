package com.olsonsolution.common.spring.domain.service.tenant;

import com.olsonsolution.common.spring.domain.port.repository.context.LocalContextManager;
import com.olsonsolution.common.spring.domain.port.repository.tenant.TenantContextAware;
import com.olsonsolution.common.spring.domain.port.repository.tenant.TenantContextAwaresConfigurer;
import com.olsonsolution.common.spring.domain.port.sterotype.context.TenantContext;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class TenantContextAwareConfigurationService implements TenantContextAwaresConfigurer {

    private final LocalContextManager localContextManager;

    private final List<TenantContextAware> tenantContextAwares;

    @Override
    public void configureAwares() {
        Optional<TenantContext> tenantContext = localContextManager.obtainThreadLocalAs(TenantContext.class);
        if (tenantContext.isPresent()) {
            for (TenantContextAware aware : tenantContextAwares) {
                aware.configure(tenantContext.get());
            }
        }
    }
}
