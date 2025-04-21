package com.olsonsolution.common.spring.application.datasource.migration.annotation.processor;

/**
 * <changeSet id="2" author="you">
 *     <dropDefaultValue
 *         tableName="your_table"
 *         columnName="your_column"/>
 * </changeSet>
 */
record DropDefaultValueOp(String table, String column) implements ChangeSetOperation {
}
