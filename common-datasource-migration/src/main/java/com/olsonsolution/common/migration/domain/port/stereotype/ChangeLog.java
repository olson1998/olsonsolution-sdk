package com.olsonsolution.common.migration.domain.port.stereotype;

import java.io.Serializable;
import java.util.Map;

public interface ChangeLog extends Serializable {

    String getPath();

    Map<String, SchemaConfig> getSchemas();

}
