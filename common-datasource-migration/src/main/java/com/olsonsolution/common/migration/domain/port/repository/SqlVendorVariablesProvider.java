package com.olsonsolution.common.migration.domain.port.repository;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;

import java.util.Map;

public interface SqlVendorVariablesProvider {

    Map<SqlVendor, Map<String, String>> getMigrationVariables();

}
