package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.olsonsolution.common.reflection.domain.port.repository.annotion.processor.MessagePrinter;
import com.olsonsolution.common.reflection.domain.port.repository.annotion.processor.TypeElementUtils;
import com.olsonsolution.common.spring.application.annotation.migration.ColumnChange;
import com.olsonsolution.common.spring.application.annotation.migration.ForeignKey;
import com.olsonsolution.common.spring.application.annotation.migration.Operation;
import com.olsonsolution.common.spring.application.annotation.migration.Param;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.SequenceGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.*;
import org.hibernate.type.spi.TypeConfiguration;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.lang.reflect.InvocationTargetException;
import java.sql.JDBCType;
import java.sql.Types;
import java.util.*;

import static java.util.Map.entry;

@RequiredArgsConstructor
class LiquibaseUtils {

    private static final Map<Integer, String> CODE_TO_LIQUIBASE = Map.ofEntries(
            // ==== Standard JDBC Types ====
            entry(Types.CHAR, "char"),
            entry(Types.NCHAR, "nchar"),
            entry(Types.VARCHAR, "varchar"),
            entry(Types.NVARCHAR, "nvarchar"),
            entry(Types.BINARY, "binary"),
            entry(Types.VARBINARY, "varbinary"),
            entry(Types.LONGVARCHAR, "longvarchar"),
            entry(Types.LONGNVARCHAR, "longnvarchar"),
            entry(Types.CLOB, "clob"),
            entry(Types.NCLOB, "nclob"),
            entry(Types.BLOB, "blob"),
            entry(Types.BOOLEAN, "boolean"),
            entry(Types.BIT, "bit"),
            entry(Types.TINYINT, "tinyint"),
            entry(Types.SMALLINT, "smallint"),
            entry(Types.INTEGER, "integer"),
            entry(Types.BIGINT, "bigint"),
            entry(Types.REAL, "real"),
            entry(Types.FLOAT, "float"),
            entry(Types.DOUBLE, "double"),
            entry(Types.DECIMAL, "decimal"),
            entry(Types.NUMERIC, "numeric"),
            entry(Types.DATE, "date"),
            entry(Types.TIME, "time"),
            entry(Types.TIME_WITH_TIMEZONE, "time with time zone"),
            entry(Types.TIMESTAMP, "timestamp"),
            entry(Types.TIMESTAMP_WITH_TIMEZONE, "timestamp with time zone"),
            entry(Types.ROWID, "rowid"),
            entry(Types.SQLXML, "xml"),
            entry(3100, "uuid"),
            entry(3101, "json"),
            entry(3102, "geography"),
            entry(3103, "geometry")
    );

    private final MessagePrinter messagePrinter;

    private final TypeElementUtils typeElementUtils;

    private final TypeConfiguration typeConfiguration = new TypeConfiguration();

    ChangeOp buildColumnOp(VariableElement fieldElement,
                           String jpaSpec,
                           String tableName,
                           String columnName,
                           Column column,
                           boolean isIdentifier) {
        ChangeOp.Builder addColumnOp = ChangeOp.builder()
                .operation("column")
                .attribute("name", columnName)
                .attribute("type", getLiquibaseType(fieldElement, column));
        ChangeOp.Builder constraints = null;
        if (isIdentifier) {
            constraints = ChangeOp.builder()
                    .operation("constraints")
                    .attribute("primaryKey", String.valueOf(true))
                    .attribute("primaryKeyName", "pk_" + tableName);
        }
        if (column != null) {
            if (column.unique()) {
                if (constraints == null) {
                    constraints = ChangeOp.builder()
                            .operation("constraints");
                }
                constraints.attribute("unique", String.valueOf(true));
                constraints.attribute("uniqueConstraintName", "unique_" + tableName + '_' + columnName);
            }
            if (!column.nullable()) {
                if (constraints == null) {
                    constraints = ChangeOp.builder()
                            .operation("constraints");
                }
                constraints.attribute("nullable", String.valueOf(false));
                constraints.attribute("notNullConstraintName", "nonnull_" + tableName + '_' + columnName);
            }
        }
        if (fieldElement.getAnnotation(ForeignKey.class) != null) {
            if (constraints == null) {
                constraints = ChangeOp.builder()
                        .operation("constraints");
            }
            ForeignKey foreignKey = fieldElement.getAnnotation(ForeignKey.class);
            String foreginKeyName = foreignKey.name();
            if (StringUtils.isEmpty(foreginKeyName)) {
                foreginKeyName = "fk_" + tableName + '_' + columnName;
            }
            constraints.attribute("foreignKeyName", foreginKeyName);
            constraints.attribute("referencedTableName", foreignKey.referenceTable());
            constraints.attribute("referencedColumnNames", foreignKey.referenceColumn());
            String schemaVariable = "${" + jpaSpec + "Schema}";
            if (StringUtils.isNotEmpty(foreignKey.referenceJpaSpec())) {
                schemaVariable = "${" + foreignKey.referenceJpaSpec() + "Schema}";
            }
            constraints.attribute("referencedTableSchemaName", schemaVariable);
        }
        if (constraints != null) {
            addColumnOp.childOperation(constraints.build());
        }
        return addColumnOp.build();
    }

    List<ChangeOp> buildChangeOps(Operation operation,
                                  String jpaSpec, String tableName, String columnName,
                                  ColumnChange columnChange, Column column, VariableElement entityField) {
        if (operation == Operation.ADD_COLUMN) {
            ChangeOp columnOp = buildColumnOp(entityField, jpaSpec, tableName, columnName, column, false);
            ChangeOp addColumn = ChangeOp.builder()
                    .operation("addColumn")
                    .attribute("tableName", tableName)
                    .attribute("schemaName", "${" + jpaSpec + "Schema}")
                    .childOperation(columnOp)
                    .build();
            return Collections.singletonList(addColumn);
        } else if (operation == Operation.ADD_UNIQUE_CONSTRAINT) {
            String columnNames = getParameter(columnChange, "columnNames");
            String constraintName = getParameter(columnChange, "constraintName");
            constraintName = constraintName == null ?
                    "unique_" + tableName + '_' + StringUtils.replace(columnNames, ",", "_")
                    : constraintName;
            ChangeOp addUniqueConstraint = ChangeOp.builder()
                    .operation("addUniqueConstraint")
                    .attribute("schemaName", "${" + jpaSpec + "Schema}")
                    .attribute("tableName", tableName)
                    .attribute("columnNames", columnNames)
                    .attribute("constraintName", constraintName)
                    .build();
            return Collections.singletonList(addUniqueConstraint);
        } else if (operation == Operation.ADD_NOT_NULL_CONSTRAINT) {
            String dataType = getParameter(columnChange, "columnDataType");
            ChangeOp addNotNull = ChangeOp.builder()
                    .operation("addNotNullConstraint")
                    .attribute("tableName", tableName)
                    .attribute("columnName", columnName)
                    .attribute("schemaName", "${" + jpaSpec + "Schema}")
                    .attribute("columnDataType", dataType)
                    .build();
            return Collections.singletonList(addNotNull);
        } else if (operation == Operation.DROP_DEFAULT_VALUE) {
            ChangeOp dropDefaultValue = ChangeOp.builder()
                    .operation("dropDefaultValue")
                    .attribute("table", tableName)
                    .attribute("column", columnName)
                    .build();
            return Collections.singletonList(dropDefaultValue);
        } else if (operation == Operation.DEFAULT_VALUE_CHANGE) {
            List<ChangeOp> operations = new LinkedList<>();
            ChangeOp dropDefaultValue = ChangeOp.builder()
                    .operation("dropDefaultValue")
                    .attribute("tableName", tableName)
                    .attribute("columnName", columnName)
                    .attribute("schemaName", "${" + jpaSpec + "Schema}")
                    .build();
            ChangeOp addDefaultValue = ChangeOp.builder()
                    .operation("addDefaultValue")
                    .attribute("tableName", tableName)
                    .attribute("columnName", columnName)
                    .attribute("schemaName", "${" + jpaSpec + "Schema}")
                    .build();
            operations.add(dropDefaultValue);
            operations.add(addDefaultValue);
            return operations;
        } else if (operation == Operation.DROP_NULL_CONSTRAINT) {
            ChangeOp dropNotNullConstraint = ChangeOp.builder()
                    .operation("dropNotNullConstraint")
                    .attribute("columnName", columnName)
                    .attribute("tableName", tableName)
                    .attribute("schemaName", "${" + jpaSpec + "Schema}")
                    .build();
            return Collections.singletonList(dropNotNullConstraint);
        } else if (operation == Operation.MODIFY_DATA_TYPE) {
            String newDataType = getParameter(columnChange, "newDataType");
            ChangeOp modifyDataType = ChangeOp.builder()
                    .operation("modifyDataType")
                    .attribute("columnName", columnName)
                    .attribute("tableName", tableName)
                    .attribute("schemaName", "${" + jpaSpec + "Schema}")
                    .attribute("newDataType", newDataType)
                    .build();
            return Collections.singletonList(modifyDataType);
        } else {
            return Collections.emptyList();
        }
    }

    ChangeOp parseSequenceGenerator(String jpaSpec, SequenceGenerator sequenceGenerator) {
        String schema = sequenceGenerator.schema();
        if (StringUtils.isEmpty(schema)) {
            schema = "${" + jpaSpec + "Schema}";
        }
        return ChangeOp.builder()
                .operation("createSequence")
                .attribute("sequenceName", sequenceGenerator.name())
                .attribute("schemaName", schema)
                .attribute("startValue", String.valueOf(sequenceGenerator.initialValue()))
                .attribute("incrementBy", String.valueOf(sequenceGenerator.allocationSize()))
                .build();
    }

    ChangeOp buildCreateTable(String jpaSpec, String table, Collection<ChangeOp> columns) {
        return ChangeOp.builder()
                .operation("createTable")
                .attribute("schemaName", "${" + jpaSpec + "Schema}")
                .attribute("tableName", table)
                .childOperations(columns)
                .build();
    }

    private String getLiquibaseType(VariableElement entityFieldElement, Column column) {
        JdbcType jdbcType = null;
        Integer length = null;
        Integer scale = null;
        Integer precision = null;
        if (column != null) {
            length = column.length();
            scale = column.scale();
            precision = column.precision();
        }
        if (entityFieldElement.getAnnotation(org.hibernate.annotations.JdbcType.class) != null) {
            org.hibernate.annotations.JdbcType jdbcTypeAnno =
                    entityFieldElement.getAnnotation(org.hibernate.annotations.JdbcType.class);
            jdbcType = getFromAnnotation(jdbcTypeAnno);
        } else if (entityFieldElement.getAnnotation(JdbcTypeCode.class) != null) {
            jdbcType = getJdbcType(entityFieldElement.getAnnotation(JdbcTypeCode.class));
        } else if (entityFieldElement.asType() instanceof DeclaredType declaredField &&
                declaredField.asElement() instanceof TypeElement entityFieldTypeElement) {
            if (entityFieldTypeElement.getKind() == ElementKind.ENUM) {
                Enumerated enumerated = entityFieldElement.getAnnotation(Enumerated.class);
                EnumType enumTypeValue = enumerated == null ? EnumType.ORDINAL : enumerated.value();
                jdbcType = getEnumJdbcType(enumTypeValue);
            } else {
                jdbcType = getRecommendedJdbcType(entityFieldTypeElement);
            }
        }
        if (jdbcType == null) {
            return null;
        }
        return getLiquibaseDataType(jdbcType, length, precision, scale);
    }

    private JdbcType getFromAnnotation(org.hibernate.annotations.JdbcType jdbcTypeAnnotation) {
        try {
            Class<? extends JdbcType> type = jdbcTypeAnnotation.value();
            return ConstructorUtils.invokeConstructor(type);
        } catch (NoSuchMethodException | InvocationTargetException |
                 InstantiationException | IllegalAccessException e) {
            messagePrinter.print(
                    Diagnostic.Kind.ERROR, LiquibaseUtils.class,
                    "Jdbc type can not be resolved. No constructor found for jdbc type", e
            );
            return null;
        } catch (MirroredTypeException e) {
            return getAnnotationJdbcType(e.getTypeMirror());
        }
    }

    private JdbcType getRecommendedJdbcType(TypeElement entityFieldType) {
        try {
            Class<?> javaClass = Class.forName(entityFieldType.getQualifiedName().toString());
            JavaType<?> javaTypeDescriptor = typeConfiguration.getJavaTypeRegistry().getDescriptor(javaClass);
            JdbcTypeIndicators indicators = new GenericJdbcTypeIndicators(typeConfiguration);
            return javaTypeDescriptor.getRecommendedJdbcType(indicators);
        } catch (Exception e) {
            messagePrinter.print(
                    Diagnostic.Kind.WARNING, LiquibaseUtils.class,
                    "Column type could not be resolved", e
            );
            return null;
        }
    }

    private JdbcType getEnumJdbcType(EnumType enumType) {
        if (enumType == EnumType.ORDINAL) {
            return new IntegerJdbcType();
        } else if (enumType == EnumType.STRING) {
            return new VarcharJdbcType();
        } else {
            throw new IllegalArgumentException("Unsupported enum type: %s".formatted(enumType));
        }
    }

    private JdbcType getJdbcType(JdbcTypeCode jdbcTypeCode) {
        return typeConfiguration.getJdbcTypeRegistry().getDescriptor(jdbcTypeCode.value());
    }

    private JdbcType getAnnotationJdbcType(TypeMirror jdbcTypeMirror) {
        TypeElement jdbcTypeElement = typeElementUtils.getClassElement(jdbcTypeMirror);
        if (jdbcTypeElement == null) {
            return null;
        }
        String jdbcClassName = jdbcTypeElement.getQualifiedName().toString();
        return getAnnotationJdbcType(jdbcClassName);
    }

    private JdbcType getAnnotationJdbcType(String jdbcClassName) {
        try {
            Class<? extends JdbcType> jdbcClass;
            Class<?> javaClass = Class.forName(jdbcClassName);
            if (JdbcType.class.isAssignableFrom(javaClass)) {
                jdbcClass = javaClass.asSubclass(JdbcType.class);
            } else {
                throw new ClassNotFoundException(
                        "Java class %s is not JdbcType".formatted(ClassUtils.getCanonicalName(javaClass))
                );
            }
            return ConstructorUtils.invokeConstructor(jdbcClass);
        } catch (ClassNotFoundException e) {
            messagePrinter.print(
                    Diagnostic.Kind.WARNING, LiquibaseUtils.class,
                    "Jdbc type %s not found in runtime".formatted(jdbcClassName), e
            );
        } catch (NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException | InstantiationException e) {
            messagePrinter.print(
                    Diagnostic.Kind.WARNING, LiquibaseUtils.class,
                    "Jdbc type %s can not be instantiate".formatted(jdbcClassName), e
            );
        }
        return null;
    }

    private boolean canSetPrecisionScale(JdbcType jdbcType) {
        return jdbcType instanceof DoubleJdbcType || jdbcType instanceof FloatJdbcType ||
                jdbcType instanceof DecimalJdbcType;
    }

    private boolean canSetLength(JdbcType jdbcType) {
        return jdbcType instanceof VarcharJdbcType || jdbcType instanceof NVarcharJdbcType |
                jdbcType instanceof VarbinaryJdbcType;
    }

    private String getParameter(ColumnChange columnChange, String parameterName) {
        return Optional.ofNullable(columnChange.params())
                .stream()
                .flatMap(Arrays::stream)
                .filter(parameter -> StringUtils.equals(parameter.name(), parameterName))
                .findFirst()
                .map(Param::value)
                .orElse(null);
    }

    private String getLiquibaseDataType(JdbcType jdbcType, Integer length, Integer precision, Integer scale) {
        int jdbcCode = jdbcType.getJdbcTypeCode();
        // Look up the base SQL name from the map
        String base = CODE_TO_LIQUIBASE.get(jdbcCode);
        if (base == null) {
            // Fallback: use JDBCType name lowercased
            base = JDBCType.valueOf(jdbcCode).getName().toLowerCase(Locale.ROOT);
        }
        // Apply length if appropriate
        if (length != null && length > 0 && canSetLength(jdbcType)) {
            return base + "(" + length + ")";
        }
        // Apply precision/scale if appropriate
        if (precision != null && precision > 0 && canSetPrecisionScale(jdbcType)) {
            if (scale != null && scale > 0) {
                return base + "(" + precision + "," + scale + ")";
            } else {
                return base + "(" + precision + ")";
            }
        }
        return base;
    }

}
