package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.spring.domain.port.repository.jpa.JpaSpecTransactionRouter;
import com.olsonsolution.common.spring.domain.port.repository.jpa.PlatformTransactionManagerDelegate;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class JpaSpecTransactionRoutingService implements JpaSpecTransactionRouter {

    private final Map<String, PlatformTransactionManagerDelegate> platformTransactionManagerDelegates;

    @Override
    public String resolveDelegate(String jpaSpec) {
        String beanName = jpaSpec + "_PlatformTransactionManagerDelegate";
        if (platformTransactionManagerDelegates.containsKey(beanName)) {
            return beanName;
        } else {
            throw new RuntimeException();
        }
    }
}
