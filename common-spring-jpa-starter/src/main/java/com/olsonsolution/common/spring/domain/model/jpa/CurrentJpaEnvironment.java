package com.olsonsolution.common.spring.domain.model.jpa;

import com.olsonsolution.common.spring.domain.port.stereotype.datasource.RoutingDataSource;
import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrentJpaEnvironment implements JpaEnvironment {

    private final Class<?> dialect;

    private final RoutingDataSource routingDataSource;

    public static CurrentJpaEnvironment fromJpaEnvironment(JpaEnvironment jpaEnvironment) {
        return CurrentJpaEnvironment.builder()
                .routingDataSource(jpaEnvironment.getDataBaseEnvironment())
                .dialect(jpaEnvironment.getDialect())
                .build();
    }

}
