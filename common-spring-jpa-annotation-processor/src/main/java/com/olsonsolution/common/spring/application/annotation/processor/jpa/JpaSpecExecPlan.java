package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import java.util.LinkedHashSet;
import java.util.List;

record JpaSpecExecPlan(List<JpaSpecProcedure> procedures,
                       LinkedHashSet<String> jpaSpecNames,
                       LinkedHashSet<String> changeLogsOrder) {

}
