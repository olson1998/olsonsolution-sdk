package com.olsonsolution.common.spring.domain.port.repository.jpa;

public interface JpaSpecTransactionRouter {

    String resolveDelegate(String jpaSpec);

}
