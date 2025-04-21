package com.olsonsolution.common.spring.application.datasource.classic.entity;

import com.olsonsolution.common.spring.application.datasource.migration.annotation.ChangeSet;
import com.olsonsolution.common.spring.application.datasource.migration.annotation.ColumnChange;
import com.olsonsolution.common.spring.application.datasource.migration.annotation.ColumnChanges;
import com.olsonsolution.common.spring.application.datasource.migration.annotation.Operation;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

@ChangeSet

@Entity
@Table(name = "TMMDTA")
public class ClassicTeamData {

    @Id
    @Column(name = "TMMID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TMMSEQ")
    @SequenceGenerator(name = "TMMSEQ", sequenceName = "TMMSEQ", allocationSize = 1)
    private Long id;

    @Column(name = "TMMCD", nullable = false, length = 7)
    private String code;

    @ColumnChanges(atBeginning = {
            @ColumnChange(operation = Operation.TYPE_CHANGE, version = "1.0.1", parameters = "varchar(255)")
    })
    @Column(name = "TMMNM", nullable = false, length = 63)
    private String name;

}
