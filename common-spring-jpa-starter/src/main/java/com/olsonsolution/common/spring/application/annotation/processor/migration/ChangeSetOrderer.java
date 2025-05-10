package com.olsonsolution.common.spring.application.annotation.processor.migration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Comparator for ChangeSetMetadata that orders change sets so that all dependencies
 * of a given change set appear before it. Unrelated sets are tie-broken by table name,
 * and if multiple change sets target the same table, they are ordered by a provided
 * version chronology map per table, with unspecified versions placed afterward and sorted naturally.
 */
class ChangeSetOrderer implements Comparator<ChangeSetMetadata> {

    private final Map<String, List<ChangeSetMetadata>> tableChangeSets;
    private final Map<String, List<String>> versionChronologyMap;

    /**
     * @param changeSets the full list of ChangeSetMetadata
     * @param versionChronologyMap a map from table name to desired version ordering;
     *                             versions not present in a list will be ordered after
     *                             those specified, by natural order.
     */
    ChangeSetOrderer(List<ChangeSetMetadata> changeSets,
                            Map<String, List<String>> versionChronologyMap) {
        this.tableChangeSets = changeSets.stream()
                .collect(Collectors.groupingBy(ChangeSetMetadata::table));
        this.versionChronologyMap = versionChronologyMap != null
                ? new HashMap<>(versionChronologyMap)
                : Collections.emptyMap();
    }

    @Override
    public int compare(ChangeSetMetadata cs1, ChangeSetMetadata cs2) {
        // 1. Dependency ordering across tables
        if (dependsOn(cs1, cs2)) {
            return  1;
        } else if (dependsOn(cs2, cs1)) {
            return -1;
        }
        // 2. Table-level ordering
        int tableCompare = cs1.table().compareTo(cs2.table());
        if (tableCompare != 0) {
            return tableCompare;
        }
        // 3. Within the same table, order by version chronology from the map
        String table = cs1.table();
        List<String> chronology = versionChronologyMap.getOrDefault(table, Collections.emptyList());

        String v1 = cs1.version();
        String v2 = cs2.version();

        int maxIndex = chronology.size();
        int idx1 = chronology.indexOf(v1);
        int idx2 = chronology.indexOf(v2);
        if (idx1 < 0) idx1 = maxIndex;
        if (idx2 < 0) idx2 = maxIndex;

        if (idx1 != idx2) {
            return Integer.compare(idx1, idx2);
        }
        // Both versions either at the same chronology position or both unspecified: natural order
        return v1.compareTo(v2);
    }

    /**
     * Checks if cs depends (transitively) on target.
     */
    private boolean dependsOn(ChangeSetMetadata cs, ChangeSetMetadata target) {
        return dependsOnRec(cs, target.table(), new HashSet<>());
    }

    /**
     * Recursive DFS over table-level dependencies.
     * @param cs the change set to check
     * @param targetTable name of the table we're checking dependency against
     * @param visited to guard against cycles
     */
    private boolean dependsOnRec(ChangeSetMetadata cs,
                                 String targetTable,
                                 Set<String> visited) {
        Set<String> deps = cs.dependsOn();
        if (deps == null || deps.isEmpty()) {
            return false;
        }
        for (String depTable : deps) {
            if (!visited.add(depTable)) {
                continue; // cycle guard
            }
            if (depTable.equals(targetTable)) {
                return true;
            }
            List<ChangeSetMetadata> nextList = tableChangeSets.get(depTable);
            if (nextList != null) {
                for (ChangeSetMetadata next : nextList) {
                    if (dependsOnRec(next, targetTable, visited)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
