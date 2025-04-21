package com.olsonsolution.common.spring.application.annotation.processor;

/**
 * <changeSet id="4" author="you">
 *     <dropForeignKeyConstraint
 *         baseTableName="your_table"
 *         constraintName="fk_constraint_name"/>
 * </changeSet>
 * @param table
 * @param constraintName
 */
record DropForeignKeyOp(String table, String constraintName) implements ChangeSetOperation {
}
