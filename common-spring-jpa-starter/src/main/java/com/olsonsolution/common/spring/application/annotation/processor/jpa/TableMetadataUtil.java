package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.olsonsolution.common.reflection.domain.port.repository.annotion.processor.MessagePrinter;
import jakarta.persistence.*;
import liquibase.database.Database;
import liquibase.database.core.MockDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.*;
import org.hibernate.type.spi.TypeConfiguration;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.lang.reflect.InvocationTargetException;
import java.sql.JDBCType;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hibernate.type.SqlTypes.*;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class TableMetadataUtil {

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

    private final ProcessingEnvironment processingEnv;

    private final Database anyDatabase = new MockDatabase();

    private final TypeConfiguration typeConfiguration = new TypeConfiguration();

    private final DataTypeFactory dataTypeFactory = DataTypeFactory.getInstance();

    String getTableName(Element entityElement) {
        return entityElement.getAnnotation(Table.class) == null ?
                entityElement.getSimpleName().toString() :
                entityElement.getAnnotation(Table.class).name();
    }

    String getColumnName(VariableElement entityFieldElement) {
        return entityFieldElement.getAnnotation(Column.class) == null ?
                entityFieldElement.getSimpleName().toString() :
                entityFieldElement.getAnnotation(Column.class).name();
    }

    String getSqlType(VariableElement entityFieldElement, Column column) {
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
        messagePrinter.print(
                Diagnostic.Kind.NOTE, TableMetadataUtil.class,
                "Entity field mirror %s resolve Jdbc Type %s".formatted(entityFieldElement, jdbcType)
        );
        return getLiquibaseDataType(jdbcType, length, precision, scale);
    }

    boolean isEmbeddable(VariableElement variableElement) {
        if (processingEnv.getTypeUtils().asElement(variableElement.asType()) instanceof TypeElement fieldTypeElement) {
            return fieldTypeElement.getAnnotation(Embeddable.class) != null;
        }
        return false;
    }

    boolean isIdentifier(VariableElement entityFieldElement) {
        return entityFieldElement.getAnnotation(Id.class) != null;
    }

    boolean isEmbeddableIdentifier(VariableElement entityFieldElement) {
        return entityFieldElement.getAnnotation(EmbeddedId.class) != null;
    }

    private JdbcType getFromAnnotation(org.hibernate.annotations.JdbcType jdbcTypeAnnotation) {
        try {
            Class<? extends JdbcType> type = jdbcTypeAnnotation.value();
            return ConstructorUtils.invokeConstructor(type);
        } catch (NoSuchMethodException | InvocationTargetException |
                 InstantiationException | IllegalAccessException e) {
            messagePrinter.print(
                    Diagnostic.Kind.ERROR, TableMetadataUtil.class,
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
                    Diagnostic.Kind.WARNING, TableMetadataUtil.class,
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
        TypeElement jdbcTypeElement = null;
        Types typeUtils = processingEnv.getTypeUtils();
        if (jdbcTypeMirror instanceof DeclaredType declaredClass &&
                typeUtils.asElement(declaredClass) instanceof TypeElement classElement) {
            jdbcTypeElement = classElement;
        }
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
                    Diagnostic.Kind.WARNING, TableMetadataUtil.class,
                    "Jdbc type %s not found in runtime".formatted(jdbcClassName), e
            );
        } catch (NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException | InstantiationException e) {
            messagePrinter.print(
                    Diagnostic.Kind.WARNING, TableMetadataUtil.class,
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

}
