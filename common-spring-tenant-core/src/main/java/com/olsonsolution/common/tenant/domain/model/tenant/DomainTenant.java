package com.olsonsolution.common.tenant.domain.model.tenant;

import com.olsonsolution.common.tenant.domain.port.sterotype.tenant.Tenant;
import lombok.Data;

@Data
public class DomainTenant implements Tenant {

    private final String id;

}
