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
    @Column(name = "PRSID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PRSSEQ")
    @SequenceGenerator(name = "PRSSEQ", sequenceName = "PRSSEQ", allocationSize = 1)
    private Long id;

    @Column(name = "PRSNM", length = 63)
    private String name;

    @Column(name = "PRSSN")
    private String surname;

    @Column(name = "PRSGN")
    private char gender;
}
