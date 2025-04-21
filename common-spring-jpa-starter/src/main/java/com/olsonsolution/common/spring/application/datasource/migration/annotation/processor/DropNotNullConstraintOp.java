package com.olsonsolution.common.spring.application.datasource.migration.annotation.processor;

/**
 * <changeSet id="1" author="you">
 *     <dropNotNullConstraint
 *         tableName="your_table"
 *         columnName="your_column"
 *         columnDataType="VARCHAR(255)"/>
 * </changeSet>
 */
record DropNotNullConstraintOp(String table, String column) implements ChangeSetOperation {
}
