package com.olsonsolution.common.spring.domain.port.props.jpa;

import java.util.Collection;

public interface JpaProperties {

    Collection<? extends EntityManagerFactoryProperties> getEntityManagerFactory();

}
