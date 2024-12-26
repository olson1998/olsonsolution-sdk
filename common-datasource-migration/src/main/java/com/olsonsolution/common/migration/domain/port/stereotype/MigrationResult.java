package com.olsonsolution.common.migration.domain.port.stereotype;

import org.joda.time.MutableDateTime;

import java.io.Serializable;

public interface MigrationResult extends Serializable {

    boolean isCreatedSchema();

    String getPath();

    MutableDateTime getStartTimestamp();

    MutableDateTime getFinishTimestamp();

}
