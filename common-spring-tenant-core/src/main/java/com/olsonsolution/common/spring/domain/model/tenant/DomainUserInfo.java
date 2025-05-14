package com.olsonsolution.common.spring.domain.model.tenant;

import com.olsonsolution.common.spring.domain.port.sterotype.tenant.UserInfo;
import lombok.Data;

import java.util.Locale;

@Data
public class DomainUserInfo implements UserInfo {

    private final String companyCode;

    private final Locale language;

}
