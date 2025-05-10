package com.olsonsolution.common.migration.domain.port.repository;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import lombok.NonNull;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

public interface SqlVendorSupporter {

    @NonNull
    SqlVendor getVendor();

    Collection<Class<? extends DataSource>> getSupportedDataSourceClasses();

    boolean existsSchema(@NonNull DataSource dataSource, String schema) throws SQLException;

    void createSchema(@NonNull DataSource dataSource, String schema) throws SQLException;

    @NonNull
    Map<String, String> getTypeVariables();

}
