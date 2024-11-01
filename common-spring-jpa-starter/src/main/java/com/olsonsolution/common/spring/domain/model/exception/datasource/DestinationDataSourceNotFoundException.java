package com.olsonsolution.common.spring.domain.model.exception.datasource;

import com.olsonsolution.common.spring.domain.port.stereotype.hibernate.DataBaseEnvironment;

public class DestinationDataSourceNotFoundException extends IllegalStateException {

    private static final String MSG = "Destination data source not found for environment %s";

    public DestinationDataSourceNotFoundException(DataBaseEnvironment dataBaseEnvironment) {
        super(MSG.formatted(dataBaseEnvironment));
    }
}
