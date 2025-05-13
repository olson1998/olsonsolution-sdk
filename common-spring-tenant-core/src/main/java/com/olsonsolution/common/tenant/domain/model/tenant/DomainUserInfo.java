package com.olsonsolution.common.tenant.domain.model.tenant;

import com.olsonsolution.common.tenant.domain.port.sterotype.tenant.UserInfo;
import lombok.Data;

import java.util.Locale;

@Data
public class DomainUserInfo implements UserInfo {

    private final String companyCode;

    private final Locale language;

}
