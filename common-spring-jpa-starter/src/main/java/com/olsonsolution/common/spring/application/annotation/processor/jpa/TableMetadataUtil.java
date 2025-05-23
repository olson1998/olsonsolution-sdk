package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.olsonsolution.common.reflection.domain.port.repository.annotion.processor.MessagePrinter;
import jakarta.persistence.*;
import liquibase.database.core.MockDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.structure.core.DataType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.*;
import org.hibernate.type.spi.TypeConfiguration;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.lang.reflect.InvocationTargetException;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class TableMetadataUtil {

    private final MessagePrinter messagePrinter;

    private final ProcessingEnvironment processingEnv;

    private final MockDatabase mockDatabase = new MockDatabase();

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
        if (column != null && StringUtils.isNotBlank(column.columnDefinition())) {
            return column.columnDefinition();
        }
        JdbcType jdbcType = null;
        if (entityFieldElement.getAnnotation(org.hibernate.annotations.JdbcType.class) != null) {
            jdbcType = getFromAnnotation(entityFieldElement.getAnnotation(org.hibernate.annotations.JdbcType.class));
        } else if (entityFieldElement.getAnnotation(JdbcTypeCode.class) != null) {
            jdbcType = getJdbcType(entityFieldElement.getAnnotation(JdbcTypeCode.class));
        }
        if (entityFieldElement.asType() instanceof TypeElement entityFieldType) {
            jdbcType = getRecommendedJdbcType(entityFieldType, column);
        }
        return jdbcType != null ? getDatabaseType(jdbcType, column) : EMPTY;
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
        Class<? extends JdbcType> type = jdbcTypeAnnotation.value();
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InvocationTargetException |
                 InstantiationException | IllegalAccessException e) {
            messagePrinter.print(
                    Diagnostic.Kind.ERROR, TableMetadataUtil.class,
                    "Jdbc type can not be resolved. No constructor foudn for jdbc type: %s".formatted(type), e
            );
            return null;
        }
    }

    private JdbcType getRecommendedJdbcType(TypeElement entityFieldType, Column column) {
        try {
            Class<?> javaType = Class.forName(entityFieldType.getQualifiedName().toString());
            JavaType<?> javaTypeDescriptor = typeConfiguration.getJavaTypeRegistry().getDescriptor(javaType);
            JdbcTypeIndicators indicators = ColumnJdbcTypeIndicator.forAnnotation(column, typeConfiguration);
            return javaTypeDescriptor.getRecommendedJdbcType(indicators);
        } catch (ClassNotFoundException e) {
            messagePrinter.print(
                    Diagnostic.Kind.ERROR, TableMetadataUtil.class,
                    "Jdbc type can not be resolved. No class for name found", e
            );
            throw new IllegalArgumentException(e);
        }
    }

    private JdbcType getJdbcType(JdbcTypeCode jdbcTypeCode) {
        return typeConfiguration.getJdbcTypeRegistry().getDescriptor(jdbcTypeCode.value());
    }

    private String getDatabaseType(JdbcType jdbcType, Column column) {
        DataType dataType = new DataType();
        dataType.setDataTypeId(jdbcType.getJdbcTypeCode());
        LiquibaseDataType liquibaseDataType = dataTypeFactory.from(dataType, mockDatabase);
        if (canSetLength(jdbcType) && column != null) {
            liquibaseDataType.addParameter(column.length());
        }
        if (canSetPrecisionScale(jdbcType) && column != null && column.precision() > 0) {
            String param = String.valueOf(column.precision());
            if (column.scale() > 0) {
                param = param + ',' + column.scale();
            }
            liquibaseDataType.addParameter(param);
        }
        return liquibaseDataType.toDatabaseDataType(mockDatabase).toString();
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
