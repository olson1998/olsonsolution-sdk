package com.olsonsolution.common.spring.domain.port.repository.jpa;

import org.springframework.transaction.PlatformTransactionManager;

public interface PlatformTransactionManagerDelegate extends PlatformTransactionManager, DataSourceSpecConfigurable<PlatformTransactionManager> {
}
