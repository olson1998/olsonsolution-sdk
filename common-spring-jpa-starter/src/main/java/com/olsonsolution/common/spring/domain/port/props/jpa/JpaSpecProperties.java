package com.olsonsolution.common.spring.domain.port.props.jpa;

import java.util.Properties;

public interface JpaSpecProperties {

    String getName();

    String getSchema();

    boolean isLogSql();

    boolean isFormatSqlLog();

    PackagesToScanProperties getEntityProperties();

    PackagesToScanProperties getJpaRepositoryProperties();

    Properties getProperties();

}
