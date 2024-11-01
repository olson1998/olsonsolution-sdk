package com.olsonsolution.common.spring.domain.model.hibernate;

import com.olsonsolution.common.spring.domain.port.stereotype.hibernate.DataBaseEnvironment;
import lombok.Data;

@Data
public class CurrentDataBaseEnvironment implements DataBaseEnvironment {

    private final String id;

    private final String dataBase;

    private final String schema;

}
