package com.olsonsolution.common.spring.application.datasource.classic.entity;

import com.olsonsolution.common.spring.application.datasource.migration.annotation.ChangeSet;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private Long id;

    @Column(name = "PRSNM")
    private String name;

    @Column(name = "PRSSN")
    private String surname;

    @Column(name = "PRSGN")
    private String gender;
}
