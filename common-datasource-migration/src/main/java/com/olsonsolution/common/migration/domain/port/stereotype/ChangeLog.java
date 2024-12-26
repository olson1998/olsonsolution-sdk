package com.olsonsolution.common.migration.domain.port.stereotype;

import java.io.Serializable;

public interface ChangeLog extends Serializable {

    String getSchema();

    String getPath();

    boolean isCreateSchema();

}
