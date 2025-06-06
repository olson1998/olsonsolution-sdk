package com.olsonsolution.common.spring.domain.port.props.datasource;

import java.util.Collection;

public interface DestinationDataSourceProperties {

    Collection<? extends SqlDataSourceProperties> getInstance();

}
