package com.olsonsolution.common.tenant.domain.model;

import com.olsonsolution.common.tenant.port.stereotype.Tenant;
import lombok.Data;

@Data
public class TenantModel implements Tenant {

    private final String id;

}
