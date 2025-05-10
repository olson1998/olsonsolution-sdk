package com.olsonsolution.common.spring.domain.service.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.spring.domain.model.exception.datasource.DataSourceException;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlVendorConverter;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlVendorResolver;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class SqlVendorConversionService implements SqlVendorConverter {

    private final List<SqlVendorResolver> vendorResolvers;

    @Override
    public SqlVendor convert(@NonNull String source) {
        return vendorResolvers.stream()
                .map(resolver -> resolver.resolve(source))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.collectingAndThen(
                        Collectors.toUnmodifiableList(),
                        vendors -> extractVendor(vendors, source)
                ));
    }

    private SqlVendor extractVendor(List<SqlVendor> vendors, String vendorName) {
        try {
            return CollectionUtils.extractSingleton(vendors);
        } catch (IllegalArgumentException e) {
            throw new DataSourceException("Expected to resolve single vendor for name: '%s', but found: %s"
                    .formatted(vendorName, vendors), e);
        }
    }
}
