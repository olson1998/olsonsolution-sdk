package com.olsonsolution.common.migration.domain.model;

import com.olsonsolution.common.migration.domain.port.stereotype.MigrationResult;
import lombok.Builder;
import lombok.Data;
import org.joda.time.MutableDateTime;

@Data
@Builder
public class DomainMigrationResult implements MigrationResult {

    private final boolean createdSchema;

    private final String path;

    private final MutableDateTime startTimestamp;

    private final MutableDateTime finishTimestamp;

}
