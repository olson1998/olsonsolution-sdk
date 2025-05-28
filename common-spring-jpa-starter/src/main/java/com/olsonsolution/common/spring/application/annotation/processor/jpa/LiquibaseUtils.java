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
import liquibase.database.Database;
import liquibase.database.core.MockDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
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
import java.util.*;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hibernate.type.SqlTypes.*;

@RequiredArgsConstructor
class LiquibaseUtils {

    public static final Map<Integer, String> CODE_TO_LIQUIBASE = Map.ofEntries(
            // ───── core “virtual” temporal + UUID ───────────────────────────────
            new DefaultMapEntry<>(UUID, "uuid"),                            // 3000
            new DefaultMapEntry<>(TIMESTAMP_UTC, "timestamp"),                      // 3003
            new DefaultMapEntry<>(TIME_UTC, "time"),                           // 3007
            new DefaultMapEntry<>(OFFSET_DATE_TIME, "timestamp with time zone"),       // 3012 :contentReference[oaicite:3]{index=3}
            new DefaultMapEntry<>(OFFSET_TIME, "time with time zone"),            // 3013 :contentReference[oaicite:4]{index=4}

            // ───── JSON / network / interval ────────────────────────────────────
            new DefaultMapEntry<>(JSON, "json"),                           // 3001 :contentReference[oaicite:5]{index=5}
            new DefaultMapEntry<>(INET, "inet"),                           // 3002 (pass-through) :contentReference[oaicite:6]{index=6}
            new DefaultMapEntry<>(INTERVAL_SECOND, "interval"),                       // 3100 :contentReference[oaicite:7]{index=7}

            // ───── LOB helpers used by some dialects ────────────────────────────
            new DefaultMapEntry<>(MATERIALIZED_BLOB, "blob"),                           // 3004 :contentReference[oaicite:8]{index=8}
            new DefaultMapEntry<>(MATERIALIZED_CLOB, "clob"),                           // 3005 :contentReference[oaicite:9]{index=9}
            new DefaultMapEntry<>(MATERIALIZED_NCLOB, "nclob"),                          // 3006 :contentReference[oaicite:10]{index=10}
            new DefaultMapEntry<>(LONG32VARBINARY, "blob"),                           // 4003 :contentReference[oaicite:11]{index=11}
            new DefaultMapEntry<>(LONG32VARCHAR, "clob"),                            // 4001 :contentReference[oaicite:12]{index=12}
            new DefaultMapEntry<>(LONG32NVARCHAR, "nclob"),                          // 4002 :contentReference[oaicite:13]{index=13}

            // ───── spatial & geo types ──────────────────────────────────────────
            new DefaultMapEntry<>(GEOMETRY, "geometry"),                       // 3200 :contentReference[oaicite:14]{index=14}
            new DefaultMapEntry<>(POINT, "point"),                          // 3201 :contentReference[oaicite:15]{index=15}
            new DefaultMapEntry<>(GEOGRAPHY, "geography"),                      // 3250 :contentReference[oaicite:16]{index=16}

            // ───── ENUM helpers (6.5+) ──────────────────────────────────────────
            new DefaultMapEntry<>(NAMED_ENUM, "varchar"),                        // 6001 :contentReference[oaicite:17]{index=17}
            new DefaultMapEntry<>(ORDINAL_ENUM, "int"),                            // 6002 :contentReference[oaicite:18]{index=18}
            new DefaultMapEntry<>(NAMED_ORDINAL_ENUM, "int")                             // 6003 :contentReference[oaicite:19]{index=19}
    );

    private final MessagePrinter messagePrinter;

    private final TypeElementUtils typeElementUtils;

    private final Database anyDatabase = new MockDatabase();

    private final TypeConfiguration typeConfiguration = new TypeConfiguration();

    private final DataTypeFactory dataTypeFactory = DataTypeFactory.getInstance();

    ChangeOp buildColumnOp(VariableElement fieldElement,
                           String jpaSpec,
                           String tableName,
                           String columnName,
                           Column column,
                           boolean isIdentifier) {
        ChangeOp.Builder addColumnOp = ChangeOp.builder()
                .operation("column")
                .attribute("name", columnName)
                .attribute("type", getLiquibaseType(fieldElement));
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

    private String getLiquibaseType(VariableElement entityFieldElement) {
        JdbcType jdbcType = null;
        Integer length = null;
        Integer scale = null;
        Integer precision = null;
        Column column = entityFieldElement.getAnnotation(Column.class);
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
        messagePrinter.print(
                Diagnostic.Kind.NOTE, JpaEntityUtil.class,
                "Entity field mirror %s resolve Jdbc Type %s".formatted(entityFieldElement, jdbcType)
        );
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
        } catch (ClassNotFoundException e) {
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

    private String getLiquibaseDataType(JdbcType jdbcType, Integer length, Integer precision, Integer scale) {
        int jdbcCode = jdbcType.getJdbcTypeCode();
        if (jdbcCode < 3000) {
            String typeDesc = JDBCType.valueOf(jdbcCode).getName();
            LiquibaseDataType liquibaseDataType = dataTypeFactory.fromDescription(typeDesc, anyDatabase);
            if (length != null && canSetLength(jdbcType)) {
                liquibaseDataType.addParameter(length);
            }
            if (precision != null && canSetPrecisionScale(jdbcType) && precision > 0) {
                String param = String.valueOf(precision);
                if (scale != null && scale > 0) {
                    param = param + ',' + scale;
                }
                liquibaseDataType.addParameter(param);
            }
            return liquibaseDataType.toDatabaseDataType(anyDatabase).getType();
        } else {
            return CODE_TO_LIQUIBASE.getOrDefault(jdbcCode, EMPTY);
        }
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

}
