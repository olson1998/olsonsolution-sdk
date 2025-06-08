package com.olsonsolution.common.spring.domain.port.repository.jpa;

import jakarta.persistence.EntityManagerFactory;

public interface EntityManagerFactoryDelegate extends
        EntityManagerFactory,
        DataSourceSpecConfigurable<EntityManagerFactory> {

}
