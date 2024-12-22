package com.olsonsolution.common.spring.domain.service.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlVendorConverter;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlVendorResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class SqlVendorConversionService implements SqlVendorConverter {

    private final List<SqlVendorResolver> vendorResolvers;

    @Override
    public SqlVendor convert(String source) {
        List<SqlVendor> resolvedVendors = vendorResolvers.stream()
                .map(resolver -> resolver.resolve(source))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        if(resolvedVendors.size() == 1) {
            return resolvedVendors.get(0);
        } else {
            throw new IllegalArgumentException("Multiple vendors resolved for name: '%s'".formatted(source));
        }
    }
}
