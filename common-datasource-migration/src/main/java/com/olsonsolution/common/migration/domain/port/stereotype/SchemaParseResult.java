package com.olsonsolution.common.migration.domain.port.stereotype;

public interface SchemaParseResult {

    boolean isSchemaExists();

    boolean isSchemaCreated();

    boolean isCreateSchemaEnabled();

}
