package com.olsonsolution.common.liquibase.domain.port.repository;

import java.util.Set;

public interface LiquibaseContextProvider {

    String getContextName();

    Set<String> getLabels();

}
