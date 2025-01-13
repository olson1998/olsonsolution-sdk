package com.olsonsolution.common.migration.domain.service;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.migration.domain.port.repository.ChangelogProvider;
import com.olsonsolution.common.migration.domain.port.repository.SqlVendorVariablesProvider;
import com.olsonsolution.common.migration.domain.port.stereotype.ChangeLog;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChangeLogUtils {

    public static Collection<? extends ChangeLog> collectChangeLogs(Collection<ChangelogProvider> providers) {
        return providers.stream()
                .map(ChangelogProvider::getChangelogs)
                .flatMap(Collection::stream)
                .toList();
    }

    public static Map<SqlVendor, Map<String, String>> collectVendorVariables(
            Collection<? extends SqlVendorVariablesProvider> providers) {
        return providers.stream()
                .flatMap(ChangeLogUtils::streamVendorVariables)
                .collect(Collectors.groupingBy(
                        KeyValue::getKey,
                        Collectors.collectingAndThen(Collectors.toList(), ChangeLogUtils::accumulateVendorVariables)
                ));
    }

    private static Stream<KeyValue<SqlVendor, KeyValue<String, String>>> streamVendorVariables(
            SqlVendorVariablesProvider provider) {
        return provider.getMigrationVariables()
                .entrySet()
                .stream()
                .flatMap(ChangeLogUtils::streamVendorVariables);
    }

    private static Stream<KeyValue<SqlVendor, KeyValue<String, String>>> streamVendorVariables(
            Map.Entry<SqlVendor, Map<String, String>> vendorVariables) {
        SqlVendor vendor = vendorVariables.getKey();
        return vendorVariables.getValue()
                .entrySet()
                .stream()
                .map(var -> new DefaultMapEntry<>(var.getKey(), var.getValue()))
                .map(var -> new DefaultMapEntry<>(vendor, var));
    }

    private static Map<String, String> accumulateVendorVariables(
            List<KeyValue<SqlVendor, KeyValue<String, String>>> vendorVariables) {
        return vendorVariables.stream()
                .map(KeyValue::getValue)
                .collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue));
    }

}
