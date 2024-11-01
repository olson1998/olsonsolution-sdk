package com.olsonsolution.common.spring.domain.port.props.jpa;

import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;

import java.util.Properties;

public interface EntityManagerFactoryProperties {

    JpaEnvironment getEnvironment();

    boolean isLogSql();

    boolean isFormatSqlLog();

    PackagesToScanProperties getEntity();

    PackagesToScanProperties getJpaRepository();

    Properties getProperties();

}
