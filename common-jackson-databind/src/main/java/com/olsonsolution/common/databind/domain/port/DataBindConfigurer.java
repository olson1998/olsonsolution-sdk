package com.olsonsolution.common.databind.domain.port;

import com.olsonsolution.common.databind.domain.port.repository.DatabindModuleSupplier;
import com.olsonsolution.common.time.domain.port.TimeUtils;
import org.joda.time.format.DateTimeFormatter;

public interface DataBindConfigurer {

    DatabindModuleSupplier configJodaDateTimeDataBindSupplier(DateTimeFormatter dateTimeFormatter);

}
