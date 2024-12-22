package com.olsonsolution.common.spring.domain.service.datasource;

import com.olsonsolution.common.data.domain.model.sql.SqlVendors;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlVendorResolver;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

public class SqlVendorsResolver implements SqlVendorResolver {

    @Override
    public Optional<SqlVendor> resolve(String name) {
        return Arrays.stream(SqlVendors.values())
                .filter(vendor -> StringUtils.equals(vendor.name(), name))
                .map(SqlVendor.class::cast)
                .findFirst();
    }
}
