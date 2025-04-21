package com.olsonsolution.common.spring.application.datasource.migration.annotation.processor;

/**
 * <changeSet id="5" author="you">
 *     <modifyDataType
 *         tableName="your_table"
 *         columnName="your_column"
 *         newDataType="VARCHAR(255)"/>
 * </changeSet>
 * @param table
 * @param column
 * @param columnDataType
 */
record ModifyDataTypeOp(String table, String column, String columnDataType) implements ChangeSetOperation {
}
