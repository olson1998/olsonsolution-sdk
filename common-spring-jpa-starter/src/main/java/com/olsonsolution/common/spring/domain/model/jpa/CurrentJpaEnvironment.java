package com.olsonsolution.common.spring.domain.model.jpa;

import com.olsonsolution.common.spring.domain.port.stereotype.hibernate.DataBaseEnvironment;
import com.olsonsolution.common.spring.domain.port.stereotype.jpa.JpaEnvironment;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrentJpaEnvironment implements JpaEnvironment {

    private final Class<?> dialect;

    private final DataBaseEnvironment dataBaseEnvironment;

    public static CurrentJpaEnvironment fromJpaEnvironment(JpaEnvironment jpaEnvironment) {
        return CurrentJpaEnvironment.builder()
                .dataBaseEnvironment(jpaEnvironment.getDataBaseEnvironment())
                .dialect(jpaEnvironment.getDialect())
                .build();
    }

}
