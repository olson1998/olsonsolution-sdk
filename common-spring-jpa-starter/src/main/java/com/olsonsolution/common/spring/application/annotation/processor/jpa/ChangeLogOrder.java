package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import org.apache.commons.collections4.keyvalue.DefaultMapEntry;

import java.util.*;
import java.util.stream.Collectors;

class ChangeLogOrder implements Comparator<String> {

    /**
     * Unmodifiable map: identifier → (direct) dependencies.
     */
    private final Map<String, Set<String>> dependencyMap;

    ChangeLogOrder(List<JpaSpecProcedure> procedures) {
        this.dependencyMap = procedures.stream()
                .flatMap(procedure -> procedure.changeSets().stream())
                .flatMap(changeSetOp -> changeSetOp.dependsOn()
                        .entrySet()
                        .stream())
                .flatMap(dependsOn -> dependsOn.getValue()
                        .stream()
                        .map(changeLogId -> new DefaultMapEntry<>(dependsOn.getKey(), changeLogId)))
                .collect(Collectors.groupingBy(
                        DefaultMapEntry::getKey,
                        Collectors.mapping(DefaultMapEntry::getValue, Collectors.toCollection(HashSet::new))
                ));
    }

    @Override
    public int compare(String a, String b) {
        if (Objects.equals(a, b)) {
            return 0;
        }
        if (dependsOn(a, b)) {
            // “a” must come AFTER “b”
            return 1;
        }
        if (dependsOn(b, a)) {
            // “b” must come AFTER “a”
            return -1;
        }
        // No dependency relationship – fall back to natural order for stability
        return a.compareTo(b);
    }

    /**
     * Checks whether {@code id} depends (directly or transitively) on
     * {@code target}.
     */
    private boolean dependsOn(String id, String target) {
        return dependsOn(id, target, new HashSet<>());
    }

    private boolean dependsOn(String id, String target, Set<String> visited) {
        if (!visited.add(id)) {
            return false; // cycle or already processed
        }
        Set<String> directDeps = dependencyMap.get(id);
        if (directDeps == null || directDeps.isEmpty()) {
            return false;
        }
        if (directDeps.contains(target)) {
            return true;
        }
        for (String dep : directDeps) {
            if (dependsOn(dep, target, visited)) {
                return true;
            }
        }
        return false;
    }
}


