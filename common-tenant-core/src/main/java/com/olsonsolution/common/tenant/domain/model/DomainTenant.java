package com.olsonsolution.common.tenant.domain.model;

import com.olsonsolution.common.tenant.domain.port.sterotype.Tenant;
import lombok.Data;

@Data
public class DomainTenant implements Tenant {

    private final String id;

}
