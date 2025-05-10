package com.olsonsolution.common.spring.application.annotation.processor;

import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;

record ChangeSetMetadata(TypeElement typeElement,
                         String table,
                         String version,
                         String changeLogPath,
                         String changelogName,
                         Set<String> dependsOn,
                         List<ChangeSetOperation> operations) {

}
