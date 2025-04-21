package com.olsonsolution.common.spring.application.datasource.migration.annotation.processor;

/**
 * <changeSet id="3" author="you">
 *     <dropUniqueConstraint
 *         constraintName="your_constraint_name"
 *         tableName="your_table"/>
 * </changeSet>
 * @param table
 * @param constraintName
 */
record DropUniqueConstraintOp(String table, String constraintName) implements ChangeSetOperation {
}
