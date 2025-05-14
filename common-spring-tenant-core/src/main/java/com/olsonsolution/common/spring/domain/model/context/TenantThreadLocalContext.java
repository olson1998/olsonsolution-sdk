package com.olsonsolution.common.spring.domain.model.context;

import com.olsonsolution.common.spring.domain.port.stereotype.context.ContextMetadata;
import com.olsonsolution.common.spring.domain.port.stereotype.context.ContextType;
import com.olsonsolution.common.spring.domain.port.sterotype.context.TenantContext;
import com.olsonsolution.common.spring.domain.port.sterotype.tenant.Tenant;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class TenantThreadLocalContext extends ThreadLocalContext implements TenantContext {

    private final Tenant tenant;

    @Builder(builderMethodName = "tenantContextBuilder")
    public TenantThreadLocalContext(String id, ContextType type, ContextMetadata metadata, Tenant tenant) {
        super(id, type, metadata);
        this.tenant = tenant;
    }

    @Override
    public String toString() {
        return "TenantThreadLocalContext(" +
                "id='" + getId() + '\'' +
                ", type='" + getType() + '\'' +
                ", metadata=" + getMetadata() +
                ", tenant=" + tenant +
                ')';
    }
}
