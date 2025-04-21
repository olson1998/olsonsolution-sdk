package com.olsonsolution.common.spring.application.annotation.processor;

import java.util.Set;

record AddUniqueConstraintOp(String table, Set<String> columns, String name) implements ChangeSetOperation {

    String columnNames() {
        return String.join(",", columns);
    }

}
