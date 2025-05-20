package com.olsonsolution.common.migration.domain.model;

import com.olsonsolution.common.migration.domain.port.stereotype.SchemaConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainSchemaConfig implements SchemaConfig {

    private boolean createSchema;

}
