package com.olsonsolution.common.spring.domain.model.exception.datasource;

import com.olsonsolution.common.spring.domain.port.stereotype.datasource.RoutingDataSource;

public class DestinationDataSourceNotFoundException extends IllegalStateException {

    private static final String MSG = "Destination data source not found for environment %s";

    public DestinationDataSourceNotFoundException(RoutingDataSource routingDataSource) {
        super(MSG.formatted(routingDataSource));
    }
}
