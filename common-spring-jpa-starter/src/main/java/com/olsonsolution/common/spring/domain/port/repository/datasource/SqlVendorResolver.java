package com.olsonsolution.common.spring.domain.port.repository.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;

import java.util.Optional;

public interface SqlVendorResolver {

    Optional<SqlVendor> resolve(String name);

}
