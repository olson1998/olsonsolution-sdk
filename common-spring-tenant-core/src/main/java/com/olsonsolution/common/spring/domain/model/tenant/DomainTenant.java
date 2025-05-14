package com.olsonsolution.common.spring.domain.model.tenant;

import com.olsonsolution.common.spring.domain.port.sterotype.tenant.Tenant;
import lombok.Data;

@Data
public class DomainTenant implements Tenant {

    private final String id;

}
