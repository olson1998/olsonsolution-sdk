package com.olsonsolution.common.spring.application.datasource.item.entity;

import com.olsonsolution.common.spring.application.annotation.migration.ChangeSet;
import com.olsonsolution.common.spring.application.annotation.migration.ColumnChange;
import com.olsonsolution.common.spring.application.annotation.migration.ColumnChanges;
import com.olsonsolution.common.spring.application.annotation.migration.Param;
import com.olsonsolution.common.spring.application.datasource.model.audit.AuditableEntity;
import com.olsonsolution.common.spring.application.jpa.service.AuditableEntityListener;
import jakarta.persistence.*;
import lombok.*;

import static com.olsonsolution.common.spring.application.annotation.migration.Operation.ADD_COLUMN;
import static com.olsonsolution.common.spring.application.annotation.migration.Operation.MODIFY_DATA_TYPE;

@Getter
@Setter
@ToString(callSuper = true)

@NoArgsConstructor

@Entity
@ChangeSet
@ColumnChanges({
        @ColumnChange(op = ADD_COLUMN, column = "code", version = "1.0.1"),
})
@Table(name = "category")
@EntityListeners({AuditableEntityListener.class})
public class CategoryData extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_id_seq")
    @SequenceGenerator(name = "category_id_seq", sequenceName = "category_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "code", nullable = false)
    private Integer code;

    @Column(name = "name", nullable = false, unique = true)
    private String fullName;

    @Column(name = "short_name", nullable = false, unique = true, length = 63)
    @ColumnChanges({
            @ColumnChange(op = MODIFY_DATA_TYPE, version = "1.0.1", params = @Param(name = "newDataType", value = "VARCHAR(127)"))
    })
    private String shortName;

    @Column(name = "description", nullable = false, length = 4095)
    private String description;

    @Builder(builderMethodName = "newCategory", builderClassName = "NewCategoryBuilder")
    public CategoryData(Integer code, String fullName, String shortName, String description) {
        this.code = code;
        this.fullName = fullName;
        this.shortName = shortName;
        this.description = description;
    }

}
