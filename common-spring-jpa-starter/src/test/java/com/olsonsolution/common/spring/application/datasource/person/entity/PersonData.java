package com.olsonsolution.common.spring.application.datasource.person.entity;

import com.olsonsolution.common.spring.application.annotation.migration.ChangeSet;
import com.olsonsolution.common.spring.application.annotation.migration.ColumnChange;
import com.olsonsolution.common.spring.application.annotation.migration.ColumnChanges;
import com.olsonsolution.common.spring.application.annotation.migration.Operation;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

@ChangeSet

@Entity
@Table(name = "person")
public class PersonData {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(generator = "seq_person_id", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "seq_person_id", sequenceName = "seq_person_id", allocationSize = 1)
    private Long id;

    @Column(name = "name", length = 63, nullable = false)
    @ColumnChanges(atBeginning = {
            @ColumnChange(
                    version = "1.0.1",
                    operation = Operation.MODIFY_DATA_TYPE,
                    parameters = @ColumnChange.Parameter(name = "newDataType", value = "varchar(4095)")
            )
    })
    private String name;

    @Column(name = "second_name", length = 63, nullable = false)
    @ColumnChanges(atBeginning = {
            @ColumnChange(
                    version = "1.0.1",
                    operation = Operation.MODIFY_DATA_TYPE,
                    parameters = @ColumnChange.Parameter(name = "newDataType", value = "varchar(4095)")
            ),
            @ColumnChange(
                    version = "1.0.1",
                    operation = Operation.ADD_NOT_NULL_CONSTRAINT,
                    parameters = @ColumnChange.Parameter(name = "columnDataType", value = "varchar(4095)")
            )
    })
    private String secondName;

    @Column(name = "surname", length = 63, nullable = false)
    @ColumnChanges(atBeginning = {
            @ColumnChange(
                    version = "1.0.1",
                    operation = Operation.MODIFY_DATA_TYPE,
                    parameters = @ColumnChange.Parameter(name = "newDataType", value = "varchar(4095)")
            )
    })
    private String surname;

    private char gender;

}
