package com.olsonsolution.common.spring.application.datasource.classic.entity;

import com.olsonsolution.common.spring.application.datasource.migration.annotation.ChangeSet;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

@ChangeSet

@Entity
@Table(name = "PRSDTA")
public class ClassicPersonData {

    @Id
    @Column(name = "PRSID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PRSSEQ")
    @SequenceGenerator(name = "PRSSEQ", sequenceName = "PRSSEQ", allocationSize = 1)
    private Long id;

    @Column(name = "PRSNM", nullable = false, length = 63)
    private String name;

    @Column(name = "PRSSN", nullable = false)
    private String surname;

    @Column(name = "PRSGN")
    private char gender;
}
