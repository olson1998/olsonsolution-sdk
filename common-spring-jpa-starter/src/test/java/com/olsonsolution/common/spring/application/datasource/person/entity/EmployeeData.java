package com.olsonsolution.common.spring.application.datasource.person.entity;

import com.olsonsolution.common.spring.application.annotation.migration.ChangeSet;
import com.olsonsolution.common.spring.application.annotation.migration.ForeignKey;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "employee")
public class EmployeeData {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(generator = "seq_employee_id", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "seq_employee_id", sequenceName = "seq_employee_id", allocationSize = 1)
    private Long id;

    @Column(name = "person_id", nullable = false)
    @ForeignKey(name = "fk_employee_person_id", referenceTable = "person", referenceColumn = "id")
    private Long personId;

    @Column(name = "team_code", nullable = false)
    private Integer teamCode;

    @Column(name = "access_key", nullable = false)
    private UUID accessKey;

}
