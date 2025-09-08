package com.olsonsolution.common.spring.domain.model.context;

import com.olsonsolution.common.spring.domain.port.stereotype.context.ContextMetadata;
import com.olsonsolution.common.spring.domain.port.stereotype.context.ContextType;
import com.olsonsolution.common.spring.domain.port.sterotype.context.TenantUserContext;
import com.olsonsolution.common.spring.domain.port.sterotype.tenant.Tenant;
import com.olsonsolution.common.spring.domain.port.sterotype.tenant.User;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TenantUserThreadLocalContext extends TenantThreadLocalContext implements TenantUserContext {

    private final User user;

    @Builder(builderMethodName = "tenantUserContextBuilder")
    public TenantUserThreadLocalContext(String id, ContextType type, ContextMetadata metadata,
                                        Tenant tenant, User user) {
        super(id, type, metadata, tenant);
        this.user = user;
    }
}
