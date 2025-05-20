package com.olsonsolution.common.migration.domain.model;

import com.olsonsolution.common.migration.domain.port.stereotype.SchemaParseResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainSchemaParseResult implements SchemaParseResult {

    private boolean schemaExists;

    private boolean schemaCreated;

    private boolean createSchemaEnabled;

    public static DomainSchemaParseResult disabled(boolean schemaExists) {
        return DomainSchemaParseResult.builder()
                .schemaExists(schemaExists)
                .schemaCreated(false)
                .createSchemaEnabled(false)
                .build();
    }

}
