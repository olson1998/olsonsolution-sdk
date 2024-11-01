package com.olsonsolution.common.spring.domain.port.stereotype.jpa;

import java.util.Properties;

public interface JpaConfig {

    String getSchema();

    Class<?> getDialect();

    Properties getProperties();

}
