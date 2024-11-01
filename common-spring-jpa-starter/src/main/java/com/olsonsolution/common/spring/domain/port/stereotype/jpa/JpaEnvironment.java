package com.olsonsolution.common.spring.domain.port.stereotype.jpa;

import com.olsonsolution.common.spring.domain.port.stereotype.hibernate.DataBaseEnvironment;

public interface JpaEnvironment {

    Class<?> getDialect();

    DataBaseEnvironment getDataBaseEnvironment();

}
